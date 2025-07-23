package es.demo.esdemo.repository;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "events")
public class EventRecord {

    @Id
    @Column(name = "uuid", nullable = false, updatable = false, unique = true)
    private String uuid;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence")
    private long sequence;
    private String stream;
    private int version;

    @Column(name = "timestamp", insertable = false, updatable = false)
    private OffsetDateTime timestamp;

    @Column(name = "event_type")
    private String type;
    private byte[] data;


    public EventRecord(String uuid, long sequence, String stream, int version, OffsetDateTime timestamp, String type, byte[] data) {
        this.uuid = uuid;
        this.sequence = sequence;
        this.stream = stream;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
        this.data = data;
    }

    public EventRecord() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long sequence() {
        return sequence;
    }

    public String stream() {
        return stream;
    }

    public int version() {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EventRecord that = (EventRecord) obj;
        return sequence == that.sequence &&
                version == that.version &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(stream, that.stream) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(type, that.type) &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(uuid, sequence, stream, version, timestamp, type);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public String toString() {
        return "EventRecord{" +
                "uuid='" + uuid + '\'' +
                ", sequence=" + sequence +
                ", stream='" + stream + '\'' +
                ", version=" + version +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }


}