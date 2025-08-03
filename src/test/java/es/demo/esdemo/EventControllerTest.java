package es.demo.esdemo;

import es.demo.esdemo.repository.WriteResult;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventControllerTest {

    private static final String TEST_STREAM = UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_TYPE = "test-type";
    private static final byte[] DUMMY_DATA = Base64.getEncoder().encode("test-data".getBytes(StandardCharsets.UTF_8));

    @LocalServerPort
    int port;

    @BeforeAll
    static void beforeAll() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void append_shouldCreateEvent() {
        var result = appendEvent(TEST_STREAM, TEST_TYPE, DUMMY_DATA);
    }

    @Test
    void appendDuplicate_shouldReturnConflict() {
        // First append
        appendEvent(TEST_STREAM, TEST_TYPE, DUMMY_DATA, 0);

        // Duplicate append
        given()
                .header("Event-Type", TEST_TYPE)
                .header("Expected-Version", 0)
                .body(DUMMY_DATA)
                .when()
                .post("/events/{streamId}", TEST_STREAM)
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void queryBySequence() {
        // Append two events
        appendEvent(TEST_STREAM, TEST_TYPE, DUMMY_DATA);
        appendEvent(TEST_STREAM, TEST_TYPE, DUMMY_DATA);

        // Query by sequence
        given()
                .accept(ContentType.JSON)
                .queryParam("sequence", 2)
                .when()
                .get("/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", equalTo(1))
                .body("[0].streamId", equalTo(TEST_STREAM))
                .body("[0].type", equalTo(TEST_TYPE));
    }

    private static WriteResult appendEvent(String streamId, String type, byte[] data, long expectedVersion) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "data", data,
                        "type", type,
                        "expectedVersion", expectedVersion
                ))
                .when()
                .post("/events/{streamId}", streamId)
                .then()
                .statusCode(200)
                .body("version", equalTo(1))
                .extract()
                .as(WriteResult.class);
    }

    private static WriteResult appendEvent(String streamId, String type, byte[] data) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "data", data,
                        "type", type
                ))
                .when()
                .post("/events/{streamId}", streamId)
                .then()
                .statusCode(200)
                .extract()
                .as(WriteResult.class);
    }

}