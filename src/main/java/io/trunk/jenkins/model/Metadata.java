package io.trunk.jenkins.model;

import org.immutables.value.Value;

@Value.Immutable
public interface Metadata {
    String token();

    static Metadata make(String token) {
        return ImmutableMetadata.builder()
                .token(token)
                .build();
    }
}
