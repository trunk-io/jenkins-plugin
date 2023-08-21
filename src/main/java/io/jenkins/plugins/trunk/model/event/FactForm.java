package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface FactForm {
    String key();

    String name();

    FactPayloadForm payload();
}
