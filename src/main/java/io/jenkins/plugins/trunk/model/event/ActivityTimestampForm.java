package io.jenkins.plugins.trunk.model.event;

import io.jenkins.plugins.trunk.model.Timestamp;
import org.immutables.value.Value;

@Value.Immutable
public interface ActivityTimestampForm {
    static ActivityTimestampForm make(String k, Timestamp v) {
        return ImmutableActivityTimestampForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    Timestamp v();
}
