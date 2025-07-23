package es.demo.esdemo.repo2;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

import static es.demo.esdemo.repo2.Constants.*;

@Repository
public class SnapshotStore {

    private static final Logger log = LoggerFactory.getLogger(SnapshotStore.class);

    private static final String SAVE_SNAPSHOT_QUERY = """
            INSERT INTO snapshots
                (stream_id, version, timestamp, data)
            VALUES
                (:stream_id, :version, now(), :data)
            ON CONFLICT (stream_id) DO
            UPDATE SET data = :data, version = :version, timestamp = now()
            """;


    private static final String LOAD_SNAPSHOT_QUERY = """
            SELECT stream_id, version, `timestamp`, `data`
            FROM snapshots s
            WHERE s.stream_id = :stream_id
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SnapshotStore(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(String streamId, int version, byte[] data) {
        int updateResult = jdbcTemplate.update(SAVE_SNAPSHOT_QUERY,
                Map.of(STREAM_ID, streamId,
                        VERSION, version,
                        DATA, Objects.requireNonNull(data)
                )
        );

        log.info("(saveSnapshot) updateResult: {}", updateResult);
    }

    public Optional<Snapshot> get(String streamId) {
        var snapshot = jdbcTemplate.queryForObject(LOAD_SNAPSHOT_QUERY, Map.of(STREAM_ID, streamId),
                (rs, rowNum) -> new Snapshot()
                        .streamId(UUID.fromString(rs.getString(STREAM_ID)))
                        .version(rs.getLong(VERSION))
                        .timestamp(rs.getTimestamp(TIMESTAMP).toLocalDateTime())
                        .data(rs.getBytes(DATA))
        );

        return Optional.ofNullable(snapshot);
    }
}