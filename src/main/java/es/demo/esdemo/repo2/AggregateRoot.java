package es.demo.esdemo.repo2;


import es.demo.esdemo.repository.exceptions.InvalidEventException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class AggregateRoot {

    protected String id;
    protected String type;
    protected long version;
    final List<Event> changes = new ArrayList<>();

    public AggregateRoot() {
    }

    public AggregateRoot(final String id, final String aggregateType) {
        this.id = id;
        this.type = aggregateType;
    }


    public abstract void when(final Event event);

    public void load(final List<Event> events) {
        events.forEach(event -> {
            this.validateEvent(event);
            this.raiseEvent(event);
            this.version++;
        });
    }

    public void apply(final Event event) {
        this.validateEvent(event);
        event.aggregateType(this.type);

        when(event);
        changes.add(event);

        this.version++;
        event.version(this.version);
    }

    public void raiseEvent(final Event event) {
        this.validateEvent(event);

        event.aggregateType(this.type);
        when(event);

        this.version++;
    }

    public void clearChanges() {
        this.changes.clear();
    }

    public void toSnapshot() {
        this.clearChanges();
    }

    private void validateEvent(final Event event) {
        if (Objects.isNull(event) || !event.streamId().equals(this.id))
            throw new InvalidEventException(event.toString());
    }

    protected Event createEvent(String eventType, byte[] data, byte[] metadata) {
        return new Event(eventType, this.type)
                .streamId(this.id)
                .version(this.version)
                .data(Objects.isNull(data) ? new byte[]{} : data)
                .metadata(Objects.isNull(metadata) ? new byte[]{} : metadata);
    }


    // ---------------


    public String id() {
        return id;
    }

    public String type() {
        return type;
    }

    public long version() {
        return version;
    }
}