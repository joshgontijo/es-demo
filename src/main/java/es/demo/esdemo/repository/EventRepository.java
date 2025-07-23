package es.demo.esdemo.repository;

import es.demo.esdemo.repo2.Event;
import es.demo.esdemo.repo2.ExpectedVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface EventRepository extends JpaRepository<EventRecord, Long> {

    Logger log = LoggerFactory.getLogger(EventRepository.class);

    int INITIAL_VERSION = 0;
    int NO_EXPECTED_VERSION = -1;

    @Query("""
            SELECT e FROM EventRecord e
            WHERE e.stream = ?1
            AND e.version >= ?2
            ORDER BY e.version ASC
            LIMIT ?3""")
    List<EventRecord> get(String stream, int startVersionInclusive, int limit);


    Stream<EventRecord> findAllBySequenceIsGreaterThanEqual(long sequenceIsGreaterThan, Pageable pageable);



    //Returns the current version of the stream, or empty if the stream does not exist
    @Query("SELECT MAX(e.version) FROM EventRecord e WHERE e.stream = ?1")
    Optional<Integer> version(String stream);


    /// Appends an event to the specified stream.
    ///
    /// @param stream          the name of the stream
    /// @param type            the type of the event
    /// @param data            the event data
    /// @param expectedVersion the expected version of the stream, semantics:
    ///                                                                      0: Append as the first event, stream must not exist
    ///                                                                     -1: no version check, always append regardless of the current version, db concurrency exceptions will be retried automatically
    /// @return a WriteResult containing the sequence number and version of the written event
    default long append(String stream, Event event, ExpectedVersion expectedVersion) {
        var expected = switch (expectedVersion) {
            case ExpectedVersion.Any any -> version(stream).orElse(INITIAL_VERSION);
            case ExpectedVersion.Of(long version) -> version;
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
                    case ExpectedVersion.Any any -> expected = currentVersion;
                    case ExpectedVersion.Of(long version) -> throw new VersionMismatch(stream, currentVersion, expected, e);
                }
            }
        }
    }
    }

    List<EventRecord> findAll(Sort sort);


//    class StudentSpecification {
//        public static Specification<Event> nameEndsWithIgnoreCase(String name) {
//            return (root, query, criteriaBuilder) ->
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase());
//        }
//
//        public static Specification<Student> isAge(int age) {
//            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("age"), age);
//        }
//
//        public static Specification<Student> isSchoolBorough(String borough) {
//            return (root, query, criteriaBuilder) -> {
//                Join<Student, School> scchoolJoin = root.join("school");
//                return criteriaBuilder.equal(scchoolJoin.get("borough"), borough);
//            };
//        }
//    }


    //-------------- Useful methods --------------------
    @Query("""
            SELECT e FROM EventRecord e \
            WHERE e.stream = ?1 \
            AND e.version >= ?2 \
            AND e.type IN ?3 \
            ORDER BY e.version ASC \
            LIMIT ?3""")
    List<EventRecord> get(String stream, int startVersionInclusive, Limit limit, Set<String> typeFilter) throws VersionMismatch;

    @Query(
            """
                    SELECT e FROM EventRecord e
                    WHERE e.timestamp BETWEEN ?1 AND ?2
                    ORDER BY e.sequence ASC
                    LIMIT ?3
                    """
    )
    List<EventRecord> findAllByTimestampBetween(OffsetDateTime start, OffsetDateTime end, Sort sort, Limit limit);

    List<EventRecord> findByType(String type, Sort sort, Limit limit);

}