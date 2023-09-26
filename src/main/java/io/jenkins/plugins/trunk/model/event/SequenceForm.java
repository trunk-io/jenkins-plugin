package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface SequenceForm {
    String platform();

    String kind();

    String key();

    String name();

    SequencePayloadForm payload();
}
