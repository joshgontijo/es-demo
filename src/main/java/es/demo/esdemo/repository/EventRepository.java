package es.demo.esdemo.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Repository
public interface EventRepository extends JpaRepository<EventRecord, Long>, JpaSpecificationExecutor<EventRecord> {

    Logger log = LoggerFactory.getLogger(EventRepository.class);

    long INITIAL_VERSION = 0;

    default WriteResult append(EventRecord event, Version expectedVersion) {
        var streamId = requireNonNull(event.streamId());

        while (true) {
            long currentVersion = version(streamId).orElse(INITIAL_VERSION);

            var expected = switch (expectedVersion) {
                case Version.Any any -> currentVersion;
                case Version.Expect(long version) when currentVersion != version ->
                        throw new VersionMismatch(streamId, currentVersion, version);
                case Version.Expect(long version) -> version;
            };

            try {
                event.version(expected + 1);
                var created = this.save(event);
                System.out.println("Saved event: " + created);
                return new WriteResult(created.sequence(), event.version());
            } catch (DataIntegrityViolationException e) {
                // This exception is thrown when the expected version does not match the DB version
                if (expectedVersion instanceof Version.Expect(long version)) {
                    //TODO: cannot possibly know the current version.
                    throw new VersionMismatch(streamId, -1, version, e);
                }
                log.warn("Version mismatch for stream {}: expected {}, current version {}, retrying...",
                        streamId, expected, currentVersion, e);
            }
        }
    }

    @Query("SELECT MAX(e.version) FROM EventRecord e WHERE e.streamId = ?1")
    Optional<Long> version(String streamId);

    //Caller must close the stream and use @Transactional(readOnly = true)
    @Query("SELECT e FROM EventRecord e WHERE e.streamId = ?1")
    Stream<EventRecord> get(String streamId);


    //------- Helper methods for aggregates and projections -------

    @Transactional(readOnly = true)
    default <T extends Aggregate> T get(String streamId, Supplier<T> instance, BiConsumer<T, EventRecord> handler) {
        return this.project(streamId, instance, (aggregate, event) -> {
            handler.accept(aggregate, event);
            aggregate.version = event.version();
        });
    }

    //TODO: remove in favour of functional approach ?
    @Transactional(readOnly = true)
    default <T> T project(String streamId, Supplier<T> instance, BiConsumer<T, EventRecord> handler) {
        T aggregate = instance.get();
        this.get(streamId).forEach(event -> handler.accept(aggregate, event));
        return aggregate;
    }

    @Transactional(readOnly = true)
    default List<EventRecord> query(Specification<EventRecord> query, Sort sort, int limit) {
        return this.findAll(query, PageRequest.of(0, limit, sort))
                .toList();
    }
}