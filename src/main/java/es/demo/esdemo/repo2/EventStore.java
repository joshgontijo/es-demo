package es.demo.esdemo.repo2;


import es.demo.esdemo.repository.VersionMismatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.*;

import static es.demo.esdemo.repo2.Constants.*;

@Repository
public class EventStore {

    private static final Logger log = LoggerFactory.getLogger(EventStore.class);

    private static final int INITIAL_VERSION = 0;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EventStore(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SAVE_EVENTS_QUERY = """
            INSERT INTO events
                (uuid, stream_id, event_type, version, timestamp, data, metadata)
            VALUES
                (:uuid, :stream_id, :event_type, :version, NOW(), :data, :metadata)
            """;
    public long append(String stream, Event event, Version expectedVersion) {
        var expected = switch (expectedVersion) {
            case Version.Any any -> version(stream).orElse(INITIAL_VERSION);
            case Version.Expect(long version) -> version;
        };

        while (true) {
            try {
                event.version(expected + 1);
                int result = jdbcTemplate.update(SAVE_EVENTS_QUERY, mapFromEvent(event));
                log.info("Saved result: {}, event: {}", result, event);
                return event.version();
            } catch (DataIntegrityViolationException e) {
                // This exception is thrown when the expected version does not match the DB version
                // (see events_stream_version_uindex DB constraint)
                // We will retry the operation with the updated current version
                //----
                var currentVersion = version(stream).orElse(INITIAL_VERSION);
                switch (expectedVersion) {
                    case Version.Any any -> expected = currentVersion;
                    case Version.Expect(long version) -> throw new VersionMismatch(stream, currentVersion, expected, e);
                }
            }
        }
    }


    public static final String LOAD_EVENTS_QUERY = """
            SELECT uuid, stream_id, event_type, version, timestamp, data, metadata
            FROM events e
            WHERE e.stream_id = :stream_id
            AND e.version >= :version
            ORDER BY e.version ASC
            """;
    public List<Event> get(String streamId, long startVersion) {
        return jdbcTemplate.query(LOAD_EVENTS_QUERY, Map.of(STREAM_ID, streamId, VERSION, startVersion),
                (rs, rowNum) -> new Event()
                        .uuid(UUID.fromString(rs.getString(UUID_COLUMN)))
                        .streamId(rs.getString(STREAM_ID))
                        .version(rs.getLong(VERSION))
                        .eventType(rs.getString(EVENT_TYPE))
                        .timestamp(rs.getTimestamp(TIMESTAMP).toLocalDateTime())
                        .data(rs.getBytes(DATA))
                        .metadata(rs.getBytes(METADATA))
        );
    }

    private static final String VERSION_QUERY = """
            SELECT MAX(version)
            FROM events
            WHERE stream_id = :stream_id
            """;

    public Optional<Integer> version(String aggregateId) {
        var version = jdbcTemplate.queryForObject(VERSION_QUERY, Map.of(AGGREGATE_ID, aggregateId), Integer.class);
        return Optional.ofNullable(version);
    }


    private Map<String, Serializable> mapFromEvent(Event event) {
        return Map.of(
                AGGREGATE_ID, event.streamId(),
                EVENT_TYPE, event.eventType(),
                DATA, Objects.isNull(event.data()) ? new byte[]{} : event.data(),
                METADATA, Objects.isNull(event.metadata()) ? new byte[]{} : event.metadata(),
                VERSION, event.version());
    }

    private void eventsBatchInsert(List<Event> events) {
        var args = events.stream().map(this::mapFromEvent).toList();
        Map<String, ?>[] maps = args.toArray(new Map[0]);
        int[] ints = jdbcTemplate.batchUpdate(SAVE_EVENTS_QUERY, maps);
        log.info("(saveEvents) BATCH saved result: {}, event: {}", ints);
    }


    public enum Order {
        ASC, DESC
    }

    private static class Query {
        private final Map<String, String> params = new HashMap<>();
        private final Order order = Order.ASC;
        private String query = """
                SELECT uuid, stream_id, event_type, version, timestamp, data, metadata
                FROM events e
                """;


    }



//    private void handleConcurrency(String streamId) {
//        try {
//            String aggregateID = jdbcTemplate.queryForObject(HANDLE_CONCURRENCY_QUERY, Map.of(STREAM_ID, streamId), String.class);
//            log.info("(handleConcurrency) aggregateID for lock: {}", aggregateID);
//        } catch (EmptyResultDataAccessException e) {
//            log.info("(handleConcurrency) EmptyResultDataAccessException: {}", e.getMessage());
//        }
//        log.info("(handleConcurrency) aggregateID for lock: {}", streamId);
//    }
}