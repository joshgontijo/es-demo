package es.demo.esdemo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import es.demo.esdemo.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.springframework.data.jpa.domain.Specification.unrestricted;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventRepository store;

    public EventController(EventRepository store) {
        this.store = store;
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record WriteRequest(
            String type,
            Optional<Long> expectedVersion,
            byte[] data,
            Optional<byte[]> metadata
    ) {
    }

    @PostMapping(value = "{streamId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WriteResult> append(
            @PathVariable String streamId,
            @RequestBody WriteRequest request) {

        var data = Base64.getEncoder().encode(request.data);
        var metadata = request.metadata()
                .map(bytes -> Base64.getEncoder().encode(bytes))
                .orElse(null);

        var event = new EventRecord()
                .streamId(streamId)
                .type(request.type())
                .data(data)
                .metadata(metadata);

        var version = request.expectedVersion().map(Version::expect).orElse(Version.any());
        var result = store.append(event, version);
        return ResponseEntity.ok(result);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventRecord>> query(
            @RequestParam(value = "sequence", required = false) Optional<Long> sequence,
            @RequestParam(value = "version", required = false) Optional<Long> version,
            @RequestParam(value = "streamId", required = false) Optional<String> streamId,
            @RequestParam(value = "fromTimestamp", required = false) Optional<Long> fromTimestamp,
            @RequestParam(value = "toTimestamp", required = false) Optional<Long> toTimestamp,
            @RequestParam(value = "type", required = false) Optional<Set<String>> types,
            @RequestParam(value = "sortBy", defaultValue = "sequence") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "ASC") Sort.Direction sortOrder,
            @RequestParam(value = "limit", defaultValue = "500") int limit
    ) {

        var timestampRange = fromTimestamp.flatMap(startTs -> toTimestamp.map(endTs -> {
            // Both a and b are present here
            return EventQuery.timestampBetween(
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(startTs), ZoneOffset.UTC),
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(endTs), ZoneOffset.UTC)
            );
        })).orElse(unrestricted());

        var withEvents = types.map(EventQuery::eventTypes).orElse(unrestricted());
        var bySequence = sequence.map(EventQuery::sequence).orElse(unrestricted());
        var byStreamId = streamId.map(EventQuery::stream).orElse(unrestricted());
        var byVersion = version.map(EventQuery::version).orElse(unrestricted());

        var query = Specification.<EventRecord>unrestricted()
                .and(timestampRange)
                .and(bySequence)
                .and(byStreamId)
                .and(byVersion)
                .and(withEvents);


        var events = store.query(query, Sort.by(sortOrder, sortBy), limit);
        return ResponseEntity.ok(events);

    }

}