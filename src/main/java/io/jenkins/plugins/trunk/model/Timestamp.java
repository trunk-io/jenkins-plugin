package io.jenkins.plugins.trunk.model;

import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
public interface Timestamp {
    static Timestamp fromEpochMs(long epochMs) {
        final var instant = Instant.ofEpochMilli(epochMs);
        return ImmutableTimestamp.builder()
                .s(instant.getEpochSecond())
                .n(instant.getNano())
                .build();
    }

    long s();

    int n();
}
