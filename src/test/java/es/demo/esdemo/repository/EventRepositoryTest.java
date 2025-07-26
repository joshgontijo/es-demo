package es.demo.esdemo.repository;

import es.demo.esdemo.repo2.Version;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static es.demo.esdemo.repository.EventQuery.*;
import static java.time.OffsetDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.data.jpa.domain.Specification.where;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventRepositoryTest {

    public static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 5);
    public static final String TEST_TYPE = "test-type";

    @Autowired
    private EventRepository eventstore;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void append() {
        long version = eventstore.append(event(TEST_STREAM), new Version.Expect(0));
        assertEquals(1, version);
    }

    @Test
    void appendDuplicate() {
        var version = eventstore.append(event(TEST_STREAM), new Version.Expect(0));
        assertEquals(1, version);

        assertThrows(VersionMismatch.class, () -> eventstore.append(event(TEST_STREAM), new Version.Expect(0)));
    }


    @Test
    void query() {
        // Arrange
        eventstore.append(event(TEST_STREAM), new Version.Any());
        eventstore.append(event(TEST_STREAM), new Version.Any());

        // Act
        var query = where(stream(TEST_STREAM))
                .and(eventTypes(Set.of(TEST_TYPE)));

        List<EventRecord> events = eventstore.query(query, Sort.by(Sort.Direction.ASC, "sequence"), 10);

        // Assert
        assertEquals(2, events.size());
    }

    @Test
    void fullQuery() {
        // Arrange
        eventstore.append(event(TEST_STREAM), new Version.Any());
        eventstore.append(event(TEST_STREAM), new Version.Any());

        // Act
        var query = where(timestampBetween(now().minus(Duration.ofDays(1)),now()))
                .and(eventTypes(Set.of(TEST_TYPE)))
                .and(EventQuery.version(0, 10))
                .and(stream(TEST_STREAM));

        List<EventRecord> events = eventstore.query(query, Sort.by(Sort.Direction.ASC, "sequence"), 10);

        // Assert
        assertEquals(2, events.size());
    }


    @Test
    void version() {
        // Arrange
        eventstore.append(event(TEST_STREAM), new Version.Expect(0));
        eventstore.append(event(TEST_STREAM), new Version.Expect(1));

        // Act
        Optional<Long> version = eventstore.version(TEST_STREAM);

        // Assert
        assertEquals(2, version.orElseThrow());
    }

    private static EventRecord event(String stream) {
        return new EventRecord()
                .streamId(stream)
                .eventType(TEST_TYPE)
                .version(0)
                .data(bytes());
    }

    private static byte[] bytes() {
        return bytes(3);
    }

    private static byte[] bytes(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (Math.random() * 256);
        }
        return data;
    }
}