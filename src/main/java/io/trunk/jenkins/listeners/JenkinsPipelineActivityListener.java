package io.trunk.jenkins.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.trunk.jenkins.ActivityHandler;
import io.trunk.jenkins.Configuration;
import io.trunk.jenkins.Mapper;
import io.trunk.jenkins.utils.JobUtil;
import io.trunk.jenkins.utils.NodeUtil;
import io.trunk.jenkins.utils.ScmUtil;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Extension
public class JenkinsPipelineActivityListener extends RunListener<Run<?, ?>> implements GraphListener {

    private static final Logger LOG = Logger.getLogger(JenkinsPipelineActivityListener.class.getName());
    private final ActivityHandler handler = new ActivityHandler();

    // List of started traces.
    // If we received event before pipeline start, we will not send it to Trunk.
    private final Cache<String, Boolean> traceStarted = CacheBuilder.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    @Override
    public void onStarted(Run<?, ?> run, @NonNull TaskListener listener) {
        traceStarted.put(Mapper.makeJobRunEventId(run), Boolean.TRUE);
        if (invalidState(run, Configuration.get())) {
            return;
        }

        final var workflowRun = JobUtil.asWorkflowRun(run);
        final var freestyleBuild = JobUtil.asFreestyleBuild(run);

        if (workflowRun != null) {
            workflowRun.getExecutionPromise().addListener(() -> {
                handler.onPipelineStarted(workflowRun, ScmUtil.getRepos(workflowRun, listener));
            }, Executors.newSingleThreadExecutor());
        } else if (freestyleBuild != null) {
            handler.onPipelineStarted(freestyleBuild, ScmUtil.getRepos(freestyleBuild, listener));
        }

    }

    @Override
    public void onFinalized(Run<?, ?> run) {
        if (this.invalidState(run, Configuration.get())) {
            return;
        }
        handler.onPipelineCompleted(run);
    }

    @Override
    public void onNewHead(FlowNode endNode) {
        if (this.invalidState(getNodeRun(endNode), Configuration.get())) {
            return;
        }

        if (isStartNode(endNode) && NodeUtil.isStageNode(endNode)) {
            handler.onStageStarted(endNode);
            return;
        }

        if (isEndNode(endNode)) {
            final var startNode = ((BlockEndNode<?>) endNode).getStartNode();
            final var timingAction = startNode.getAction(TimingAction.class);
            if (timingAction != null && NodeUtil.isStageNode(startNode)) {
                handler.onStageCompleted(startNode, endNode);
            }
        }
    }

    private static boolean isStartNode(@NonNull FlowNode node) {
        return node instanceof BlockStartNode;
    }

    private static boolean isEndNode(@NonNull FlowNode node) {
        return node instanceof BlockEndNode;
    }


    @CheckForNull
    static WorkflowRun getNodeRun(@NonNull FlowNode node) {
        try {
            return (WorkflowRun) node.getExecution().getOwner().getExecutable();
        } catch (IOException ioe) {
            LOG.warning(String.format("Failed to get run for node %s", node));
            return null;
        }
    }

    boolean invalidState(@CheckForNull Run<?, ?> run, @CheckForNull Configuration cfg) {
        if (run == null) {
            LOG.warning("Run is null");
            return true;
        }
        final var started = traceStarted.getIfPresent(Mapper.makeJobRunEventId(run));
        if (started == null || !started) {
            LOG.warning(String.format("Run %s is not started", run));
            return true;
        }
        if (cfg == null) {
            LOG.warning("Trunk configuration is not set");
            return true;
        }
        if (StringUtils.isEmpty(cfg.trunkApi)) {
            LOG.warning("Trunk API is not set");
            return true;
        }
        if (StringUtils.isEmpty(cfg.token)) {
            LOG.warning("Trunk Organization Token is not set");
            return true;
        }
        return false;
    }

}
