-- auto-generated definition
create table events
(
    uuid       CHAR(36)                           PRIMARY KEY,
    sequence   BIGINT AUTO_INCREMENT              NOT NULL,
    stream_id  VARCHAR(500)                       NOT NULL,
    event_type VARCHAR(500)                       NOT NULL,
    version    INT                                NOT NULL,
    timestamp  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data       BLOB                               NOT NULL,
    metadata   BLOB                               NULL
);


CREATE UNIQUE INDEX events_sequence_uindex
    ON events (sequence);

CREATE UNIQUE INDEX events_stream_version_uindex
    ON events (stream_id, version);

CREATE INDEX events_stream_index
    ON events (stream_id);

CREATE INDEX events_event_type_index
    ON events (event_type);

CREATE INDEX events_timestamp_index
    ON events (timestamp);

CREATE INDEX events_version_index
    ON events (version);