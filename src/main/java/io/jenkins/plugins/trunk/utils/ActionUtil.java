package io.jenkins.plugins.trunk.utils;

import hudson.model.Actionable;
import io.jenkins.plugins.trunk.InitTimeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;

public class ActionUtil {

    public static long getInitTimeMillis(Actionable node) {
        final var timingAction = node.getAction(InitTimeAction.class);
        if (timingAction != null) {
            return timingAction.getInitTime();
        }
        throw new RuntimeException("No init time action found");
    }

    public static long getStartTimeMillis(Actionable node) {
        final var timingAction = node.getAction(TimingAction.class);
        if (timingAction != null) {
            return timingAction.getStartTime();
        }
        throw new RuntimeException("No timing action found");
    }

}
