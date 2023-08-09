package io.trunk.jenkins.model;

import java.time.Instant;

public class Timestamp {
    public long seconds;
    public long nanos;

    public static Timestamp fromEpochMs(long epochMs) {
        final var instant = Instant.ofEpochMilli(epochMs);
        final var timestamp = new Timestamp();
        timestamp.seconds = instant.getEpochSecond();
        timestamp.nanos = instant.getNano();
        return timestamp;
    }
}
