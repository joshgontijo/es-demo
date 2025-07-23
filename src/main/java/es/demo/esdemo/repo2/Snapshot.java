package es.demo.esdemo.repo2;

import java.time.LocalDateTime;
import java.util.UUID;

public class Snapshot {

    private UUID streamId;
    private byte[] data;
    private long version;
    private LocalDateTime timestamp;

    public Snapshot() {
    }

    public Snapshot(UUID streamId, long version, byte[] data, LocalDateTime timestamp) {
        this.streamId = streamId;
        this.version = version;
        this.timestamp = timestamp;
        this.data = data;
    }

    public UUID streamId() {
        return streamId;
    }

    public byte[] data() {
        return data;
    }

    public long version() {
        return version;
    }

    public LocalDateTime timestamp() {
        return timestamp;
    }

    public Snapshot streamId(UUID id) {
        this.streamId = id;
        return this;
    }

    public Snapshot data(byte[] data) {
        this.data = data;
        return this;
    }

    public Snapshot version(long version) {
        this.version = version;
        return this;
    }

    public Snapshot timestamp(LocalDateTime timeStamp) {
        this.timestamp = timeStamp;
        return this;
    }
}