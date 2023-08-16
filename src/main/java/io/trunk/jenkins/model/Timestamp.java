package io.trunk.jenkins.model;

import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
public interface Timestamp {
    long seconds();

    long nanos();

    static Timestamp fromEpochMs(long epochMs) {
        final var instant = Instant.ofEpochMilli(epochMs);
        return ImmutableTimestamp.builder()
                .seconds(instant.getEpochSecond())
                .nanos(instant.getNano())
                .build();
    }
}
