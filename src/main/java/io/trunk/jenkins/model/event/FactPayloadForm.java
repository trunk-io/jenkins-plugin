package io.trunk.jenkins.model.event;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface FactPayloadForm {
    List<ActivityIntegerTagForm> tagsInt64();

    List<ActivityStringTagForm> tagsString();
}
