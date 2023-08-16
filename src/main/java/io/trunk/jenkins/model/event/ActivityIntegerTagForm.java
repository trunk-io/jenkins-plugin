package io.trunk.jenkins.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityIntegerTagForm {
    String k();

    long v();

    static ActivityIntegerTagForm make(String k, long v) {
        return ImmutableActivityIntegerTagForm.builder()
                .k(k)
                .v(v)
                .build();
    }
}
