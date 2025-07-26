package es.demo.esdemo.repository;

import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.Set;

public class EventQuery {

    public static Specification<EventRecord> eventTypes(Set<String> eventTypes) {
        return (root, query, criteriaBuilder) ->
                root.get("eventType").in(eventTypes);
    }

    public static Specification<EventRecord> timestampBetween(OffsetDateTime start, OffsetDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("timestamp"), start, end);
    }

    public static Specification<EventRecord> version(long from) {
        return version(from, Long.MAX_VALUE);
    }

    public static Specification<EventRecord> version(long from, long to) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("version"), from, to);
    }

    public static Specification<EventRecord> sequence(long from) {
        return sequence(from, Long.MAX_VALUE);
    }

    public static Specification<EventRecord> sequence(long from, long to) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("sequence"), from, to);
    }

    public static Specification<EventRecord> stream(String streamId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("streamId"), streamId);
    }

}