package io.trunk.jenkins.model;

public class Timestamp {
    public long seconds;
    public long nanos;

    public static Timestamp fromEpochMs(long epochMs) {
        final var timestamp = new Timestamp();
        timestamp.seconds = epochMs / 1000;
        timestamp.nanos = (epochMs % 1000) * 1000000;
        return timestamp;
    }
}
