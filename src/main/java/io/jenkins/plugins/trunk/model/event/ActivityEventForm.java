package io.jenkins.plugins.trunk.model.event;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.plugins.trunk.model.Timestamp;
import org.immutables.value.Value;

@Value.Immutable
public interface ActivityEventForm {
    String id();

    String platform();

    String event();

    String chainId();

    @Nullable
    String parentId();

    String origin();

    Timestamp createdAt();

    @Nullable
    Timestamp finishedAt();

    ActivityConclusion conclusion();

    ActivityPayloadForm payload();

    FactForm fact();
}
