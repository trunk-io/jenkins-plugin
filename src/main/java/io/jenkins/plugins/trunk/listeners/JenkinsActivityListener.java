package io.jenkins.plugins.trunk.listeners;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.jenkins.plugins.trunk.*;
import io.jenkins.plugins.trunk.utils.JobUtil;
import io.jenkins.plugins.trunk.utils.NodeUtil;
import io.jenkins.plugins.trunk.utils.ScmUtil;
import io.jenkins.plugins.trunk.utils.SecretsUtil;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Extension
public class JenkinsActivityListener extends RunListener<Run<?, ?>> implements GraphListener {

    private static final Logger LOG = Logger.getLogger(JenkinsActivityListener.class.getName());
    private final ActivityHandler handler = new ActivityHandler();
    private final RunTracker runTracker = new RunTracker();

    @Override
    public void onInitialize(Run<?, ?> run) {
        runTracker.trackRun(run);
        run.addAction(new InitTimeAction());
    }

    @Override
    public void onStarted(Run<?, ?> run, @NonNull TaskListener listener) {
        if (hasValidState(run, Configuration.get())) {
            run.addAction(new TimingAction());
            final var workflowRun = JobUtil.asWorkflowRun(run);
            final var freestyleBuild = JobUtil.asFreestyleBuild(run);
            if (workflowRun != null) {
                workflowRun.getExecutionPromise().addListener(() -> {
                    handler.onRunStarted(workflowRun, ScmUtil.getRepos(workflowRun, listener));
                }, Executors.newSingleThreadExecutor());
            } else if (freestyleBuild != null) {
                handler.onRunStarted(freestyleBuild, ScmUtil.getRepos(freestyleBuild, listener));
            }
        }
    }

    @Override
    public void onCompleted(Run<?, ?> run, @NonNull TaskListener listener) {
        if (this.hasValidState(run, Configuration.get())) {
            handler.onRunCompleted(run);
        }
    }

    @Override
    public void onNewHead(FlowNode endNode) {
        if (this.hasValidState(NodeUtil.getNodeRun(endNode), Configuration.get())) {
            if (NodeUtil.isStartNode(endNode) && NodeUtil.isStageNode(endNode)) {
                handler.onStageStarted(endNode);
                return;
            }
            if (NodeUtil.isEndNode(endNode)) {
                final var startNode = ((BlockEndNode<?>) endNode).getStartNode();
                final var timingAction = startNode.getAction(TimingAction.class);
                if (timingAction != null && NodeUtil.isStageNode(startNode)) {
                    handler.onStageCompleted(startNode, endNode);
                }
            }
        }
    }

    boolean hasValidState(@CheckForNull Run<?, ?> run, @CheckForNull Configuration cfg) {
        if (run == null) {
            TrunkLog.warning("Run is null");
            return false;
        }
        if (!this.runTracker.isRunTracked(run)) {
            TrunkLog.warning(String.format("Run %s is not started", run));
            return false;
        }
        if (cfg == null) {
            TrunkLog.warning("Trunk configuration is not set");
            return false;
        }
        if (StringUtils.isEmpty(cfg.trunkApi)) {
            TrunkLog.warning("Trunk API is not set");
            return false;
        }
        if (StringUtils.isEmpty(SecretsUtil.getTrunkToken())) {
            TrunkLog.warning("TRUNK_TOKEN secret not found");
            return false;
        }
        return true;
    }

}
