package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityIntegerTagForm {
    static ActivityIntegerTagForm make(String k, long v) {
        return ImmutableActivityIntegerTagForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    long v();
}
