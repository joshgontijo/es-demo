package es.demo.esdemo.repository;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "events")
public class EventRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long sequence;

    @Column(name = "stream_id")
    private String streamId;
    private long version;

    @Column(insertable = false, updatable = false)
    private OffsetDateTime timestamp;

    @Column(name = "event_type")
    private String type;
    private byte[] data;
    private byte[] metadata;


    public EventRecord(long sequence, String streamId, long version, OffsetDateTime timestamp, String type, byte[] data) {
        this.sequence = sequence;
        this.streamId = streamId;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
        this.data = data;
    }

    public EventRecord() {
    }


    public EventRecord sequence(long sequence) {
        this.sequence = sequence;
        return this;
    }

    public EventRecord streamId(String streamId) {
        this.streamId = streamId;
        return this;
    }

    EventRecord version(long version) {
        this.version = version;
        return this;
    }

    public EventRecord timestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public EventRecord data(byte[] data) {
        this.data = data;
        return this;
    }

    public EventRecord metadata(byte[] metadata) {
        this.metadata = metadata;
        return this;
    }

    public EventRecord type(String eventType) {
        this.type = eventType;
        return this;
    }

    public long sequence() {
        return sequence;
    }

    public String streamId() {
        return streamId;
    }

    public long version() {
        return version;
    }

    public OffsetDateTime timestamp() {
        return timestamp;
    }

    public String type() {
        return type;
    }

    public byte[] data() {
        return data;
    }

    public byte[] metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EventRecord that = (EventRecord) obj;
        return sequence == that.sequence &&
                version == that.version &&
                Objects.equals(streamId, that.streamId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(type, that.type) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sequence, streamId, version, timestamp, type);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "EventRecord{" +
                ", sequence=" + sequence +
                ", stream='" + streamId + '\'' +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }


}