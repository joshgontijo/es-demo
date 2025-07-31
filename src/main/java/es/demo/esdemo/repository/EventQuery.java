package es.demo.esdemo.repository;

import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Set;

public class EventQuery {

    public static final String TYPE_FIELD = "type";
    public static final String TIMESTAMP_FIELD = "timestamp";
    public static final String VERSION_FIELD = "version";
    public static final String SEQUENCE_FIELD = "sequence";
    public static final String STREAM_ID_FIELD = "streamId";

    public static Specification<EventRecord> eventTypes(Set<String> eventTypes) {
        return (root, query, criteriaBuilder) ->
                root.get(TYPE_FIELD).in(eventTypes);
    }

    public static Specification<EventRecord> timestampBetween(OffsetDateTime start, OffsetDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(TIMESTAMP_FIELD), start, end);
    }

    public static Specification<EventRecord> version(long from) {
        return version(from, Long.MAX_VALUE);
    }

    public static Specification<EventRecord> version(long from, long to) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(VERSION_FIELD), from, to);
    }

    public static Specification<EventRecord> sequence(long from) {
        return sequence(from, Long.MAX_VALUE);
    }

    public static Specification<EventRecord> sequence(long from, long to) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get(SEQUENCE_FIELD), from, to);
    }

    public static Specification<EventRecord> stream(String streamId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(STREAM_ID_FIELD), streamId);
    }

}