package io.jenkins.plugins.trunk.model.event;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface SequencePayloadForm {
    @Nullable
    List<ActivityTagForm> tags();
}
