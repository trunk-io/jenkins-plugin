package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityEventParent {
    String eventId();

    String sequenceKey();
}
