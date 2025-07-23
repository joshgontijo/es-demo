package es.demo.esdemo.repo2;


import java.time.LocalDateTime;
import java.util.UUID;

public class Event {
    private UUID uuid;
    private String streamId;
    private String eventType;
    private long version;
    private LocalDateTime timestamp;
    private byte[] data;
    private byte[] metadata;

    public Event() {
    }

    public Event(String streamId, String eventType) {
        this.uuid = UUID.randomUUID();
        this.streamId = streamId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public UUID uuid() {
        return uuid;
    }

    public Event uuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public Event streamId(String streamId) {
        this.streamId = streamId;
        return this;
    }

    public Event eventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public Event version(long version) {
        this.version = version;
        return this;
    }

    public Event data(byte[] data) {
        this.data = data;
        return this;
    }

    public Event metadata(byte[] metadata) {
        this.metadata = metadata;
        return this;
    }

    public Event timestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String streamId() {
        return streamId;
    }

    public String eventType() {
        return eventType;
    }

    public long version() {
        return version;
    }

    public byte[] data() {
        return data;
    }

    public byte[] metadata() {
        return metadata;
    }

    public LocalDateTime timestamp() {
        return timestamp;
    }
}