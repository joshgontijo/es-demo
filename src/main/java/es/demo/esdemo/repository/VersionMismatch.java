package es.demo.esdemo.repository;

public class VersionMismatch extends RuntimeException {

    public final String stream;
    public final long currentVersion;
    public final long expectedVersion;

    public VersionMismatch(String stream, long currentVersion, long expectedVersion) {
        super("Version mismatch for stream '" + stream + "', expected " + expectedVersion + ", current version: " + currentVersion);
        this.stream = stream;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
    }

    public VersionMismatch(String stream, long currentVersion, long expectedVersion, Throwable cause) {
        this.stream = stream;
        this.currentVersion = currentVersion;
        this.expectedVersion = expectedVersion;
        super.initCause(cause);
    }
}