package io.jenkins.plugins.trunk.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityMetricForm {
    static ActivityMetricForm make(String k, double v) {
        return ImmutableActivityMetricForm.builder()
                .k(k)
                .v(v)
                .build();
    }

    String k();

    double v();
}
