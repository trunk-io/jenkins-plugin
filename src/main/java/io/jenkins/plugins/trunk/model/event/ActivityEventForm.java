package io.jenkins.plugins.trunk.model.event;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.jenkins.plugins.trunk.model.Timestamp;
import org.immutables.value.Value;

@Value.Immutable
public interface ActivityEventForm {
    String id();

    @Nullable
    ActivityEventParent parent();

    String chainId();

    String origin();

    Timestamp createdAt();

    @Nullable
    Timestamp finishedAt();

    ActivityConclusion conclusion();

    ActivityPayloadForm payload();

    SequenceForm sequence();
}
