package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityTagForm {
    static ActivityTagForm make(String k, String v) {
        return ImmutableActivityTagForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    String v();
}
