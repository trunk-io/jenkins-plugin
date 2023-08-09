package io.trunk.jenkins.model.event;

public enum ActivityConclusion {
    UNSPECIFIED,
    SUCCESS,
    FAILURE,
    NEUTRAL,
    CANCELLED,
    TIMED_OUT,
    ACTION_REQUIRED,
    STALE,
    SKIPPED,
    STARTUP_FAILURE
}
