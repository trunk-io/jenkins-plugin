package io.trunk.jenkins.model.event;

import io.trunk.jenkins.model.Timestamp;
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
