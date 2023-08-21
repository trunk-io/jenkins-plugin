package org.trunk.jenkins.utils;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.FreeStyleBuild;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public class JobUtil {

    @CheckForNull
    public static WorkflowRun asWorkflowRun(@NonNull Run<?, ?> run) {
        if (run instanceof WorkflowRun) {
            return (WorkflowRun) run;
        }
        return null;
    }

    @CheckForNull
    public static FreeStyleBuild asFreestyleBuild(@NonNull Run<?, ?> run) {
        if (run instanceof FreeStyleBuild) {
            return (FreeStyleBuild) run;
        }
        return null;
    }

}
