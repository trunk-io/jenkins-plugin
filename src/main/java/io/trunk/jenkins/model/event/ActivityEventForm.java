package io.trunk.jenkins.model.event;

import io.trunk.jenkins.model.Timestamp;
import org.immutables.value.Value;

@Value.Immutable
public interface ActivityEventForm {
    String id();
    String chainId();
    String parentId();
    ActivityKind kind();
    String origin();
    Timestamp createdAt();
    Timestamp finishedAt();
    ActivityConclusion conclusion();
    ActivityPayloadForm payload();
    FactForm fact();
}
