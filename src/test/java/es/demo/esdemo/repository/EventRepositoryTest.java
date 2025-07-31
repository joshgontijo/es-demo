package es.demo.esdemo.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static es.demo.esdemo.repository.EventQuery.*;
import static java.time.OffsetDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.jpa.domain.Specification.where;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventRepositoryTest {

    public static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 5);
    public static final String TEST_TYPE = "test-type";
    public static final byte[] DUMMY_DATA = "test-data".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private EventRepository db;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void append() {
        long version = db.append(event(TEST_STREAM), new Version.Expect(0));
        assertEquals(1, version);
    }

    @Test
    void appendDuplicate() {
        var version = db.append(event(TEST_STREAM), new Version.Expect(0));
        assertEquals(1, version);

        assertThrows(VersionMismatch.class, () -> db.append(event(TEST_STREAM), new Version.Expect(0)));
    }


    @Test
    void query() {
        db.append(event(TEST_STREAM), new Version.Any());
        db.append(event(TEST_STREAM), new Version.Any());

        var query = where(stream(TEST_STREAM))
                .and(eventTypes(Set.of(TEST_TYPE)));

        List<EventRecord> events = db.query(query, Sort.by(Sort.Direction.ASC, "sequence"), 10);

        assertEquals(2, events.size());
    }

    @Test
    void fullQuery() {
        // Arrange
        db.append(event(TEST_STREAM), new Version.Any());
        db.append(event(TEST_STREAM), new Version.Any());

        // Act
        var query = where(timestampBetween(now().minus(Duration.ofDays(1)), now()))
                .and(eventTypes(Set.of(TEST_TYPE)))
                .and(EventQuery.version(0, 10))
                .and(stream(TEST_STREAM));

        var events = db.query(query, Sort.by(Sort.Direction.ASC, "sequence"), 10);

        // Assert
        assertEquals(2, events.size());
    }


    @Test
    void version() {
        // Arrange
        db.append(event(TEST_STREAM), new Version.Expect(0));
        db.append(event(TEST_STREAM), new Version.Expect(1));

        // Act
        Optional<Long> version = db.version(TEST_STREAM);

        // Assert
        assertEquals(2, version.orElseThrow());
    }

    @Test
    void get() {
        db.append(event(TEST_STREAM), new Version.Any());
        db.append(event(TEST_STREAM), new Version.Any());

//        var aggregate = db.get(TEST_STREAM, TestAggregate::new);
//        assertEquals(new String(DUMMY_DATA), aggregate.value);
//        assertEquals(2, aggregate.version());
        fail("TODO fix");
    }


    public static class TestAggregate extends Aggregate {

        private String value;

//        @Override
//        public void when(EventRecord event) {
//            if (TEST_TYPE.equals(event.type())) {
//                this.value = new String(event.data());
//            } else {
//                throw new IllegalArgumentException("Unknown event type: " + event.type());
//            }
//        }
    }

    private static EventRecord event(String stream) {
        return new EventRecord()
                .streamId(stream)
                .type(TEST_TYPE)
                .data(DUMMY_DATA);
    }
}