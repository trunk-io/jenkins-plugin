package org.trunk.jenkins.model;

import org.immutables.value.Value;

@Value.Immutable
public interface Metadata {
    static Metadata make(String token) {
        return ImmutableMetadata.builder()
                .token(token)
                .build();
    }

    String token();
}
