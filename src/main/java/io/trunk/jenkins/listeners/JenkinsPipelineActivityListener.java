package io.trunk.jenkins.listeners;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import io.trunk.jenkins.ActivityHandler;
import io.trunk.jenkins.utils.NodeUtil;
import io.trunk.jenkins.utils.ScmUtil;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.GraphListener;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Extension
public class JenkinsPipelineActivityListener extends RunListener<WorkflowRun> implements GraphListener {

    private static final Logger LOG = Logger.getLogger(JenkinsPipelineActivityListener.class.getName());
    private final ActivityHandler handler = new ActivityHandler();

    @Override
    public void onStarted(WorkflowRun run, @NonNull TaskListener listener) {
        LOG.info("On Started FIRED");
        run.getExecutionPromise().addListener(() -> {
            handler.onPipelineStarted(run, ScmUtil.getRepos(run, listener));
        }, Executors.newSingleThreadExecutor());
    }

    @Override
    public void onFinalized(WorkflowRun run) {
        LOG.info("On Finalized FIRED");
        handler.onPipelineCompleted(run);
    }

    @Override
    public void onNewHead(FlowNode endNode) {

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

    private static boolean isStartNode(FlowNode node) {
        return node instanceof BlockStartNode;
    }

    private static boolean isEndNode(FlowNode node) {
        return node instanceof BlockEndNode;
    }

}
