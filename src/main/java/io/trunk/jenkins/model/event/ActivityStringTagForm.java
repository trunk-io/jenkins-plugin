package io.trunk.jenkins.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityStringTagForm {
    String k();

    String v();

    static ActivityStringTagForm make(String k, String v) {
        return ImmutableActivityStringTagForm.builder()
                .k(k)
                .v(v)
                .build();
    }
}
