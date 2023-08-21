package org.trunk.jenkins.model.event;

import org.trunk.jenkins.model.TimestampTag;
import org.immutables.value.Value;

@Value.Immutable
public interface ActivityTimestampForm {
    static ActivityTimestampForm make(String k, TimestampTag v) {
        return ImmutableActivityTimestampForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    TimestampTag v();
}
