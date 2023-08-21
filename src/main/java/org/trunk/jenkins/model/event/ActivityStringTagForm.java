package org.trunk.jenkins.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityStringTagForm {
    static ActivityStringTagForm make(String k, String v) {
        return ImmutableActivityStringTagForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    String v();
}
