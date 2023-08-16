package io.trunk.jenkins.model.event;

import org.immutables.value.Value;

@Value.Immutable
public interface ActivityMetricForm {
    String k();

    double v();

    static ActivityMetricForm make(String k, double v) {
        return ImmutableActivityMetricForm.builder()
                .k(k)
                .v(v)
                .build();
    }
}
