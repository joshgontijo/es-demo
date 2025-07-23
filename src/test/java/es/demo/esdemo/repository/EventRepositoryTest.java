package es.demo.esdemo.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventRepositoryTest {

    public static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 5);
    public static final String TEST_TYPE = "test-type";

    @Autowired
    private EventRepository eventstore;


    //TODO also test with concurrent writes so an actual SQL exception is thrown
    @Test
    void multiThreadedAppendDuplicatedTest() {
        var tasks = new ArrayList<>();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {


            var error = new AtomicReference<Exception>();

            for (int i = 0; i < 10; i++) {
                var result = CompletableFuture.runAsync(() -> {
                    int count = 0;
                    while (error.get() == null) {
                        try {
                            eventstore.append("aaa", TEST_TYPE, bytes(), count);
                            count++;
                        } catch (VersionMismatch e) {
                            //ignore
                        }
                        catch (Exception e) {
                            error.set(e);
                        }
                    }
                });
                tasks.add(result);
            }
        }

        CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new)).join();


//        assertThrows(VersionMismatch.class, () -> {
//            eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
//            eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
//        });
    }


    @Test
    void appendDuplicate() {
        var res = eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
        assertEquals(1, res.sequence());

        assertThrows(VersionMismatch.class, () -> {
            eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
        });

    }

    @Test
    void appendReturnsSequenceNumber() {
        var result = eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
        assertEquals(1, result.sequence());

        result = eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 1);
        assertEquals(2, result.sequence());
    }


    @Test
    void get() {
        // Arrange
        eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
        eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 1);

        // Act
        List<EventRecord> events = eventstore.get(TEST_STREAM, 0, 10);

        // Assert
        assertEquals(2, events.size());
    }

    @Test
    void version() {
        // Arrange
        String streamName = "test-stream";
        eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 0);
        eventstore.append(TEST_STREAM, TEST_TYPE, bytes(), 1);

        // Act
        Optional<Integer> version = eventstore.version(streamName);

        // Assert
        assertEquals(2, version.orElseThrow());
    }

    @Test
    void append() {
        // Arrange
        String streamName = "test-stream";
        long version = 1;
        String type = "type1";

        // Act
        var result = eventstore.append(streamName, type, bytes(), 0);

        // Assert
        assertNotNull(result);
        assertEquals(version, result.version());
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