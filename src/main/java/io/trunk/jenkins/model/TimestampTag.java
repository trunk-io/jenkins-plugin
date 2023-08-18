package io.trunk.jenkins.model;

import org.immutables.value.Value;

import java.time.Instant;

@Value.Immutable
public interface TimestampTag {
    static TimestampTag fromEpochMs(long epochMs) {
        final var instant = Instant.ofEpochMilli(epochMs);
        return ImmutableTimestampTag.builder()
                .s(instant.getEpochSecond())
                .n(instant.getNano())
                .build();
    }

    long s();

    int n();
}
