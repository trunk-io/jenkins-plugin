package io.trunk.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
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
    private final JenkinsActivityHandler handler = new JenkinsActivityHandler();

    @Override
    public void onStarted(WorkflowRun run, @NonNull TaskListener listener) {
        run.getExecutionPromise().addListener(() -> {
            handler.onPipelineStarted(run, ScmUtil.getRepos(run, listener));
        }, Executors.newSingleThreadExecutor());
    }

    @Override
    public void onFinalized(WorkflowRun run) {
        handler.onPipelineCompleted(run);
    }

    @Override
    public void onNewHead(FlowNode node) {

        if (isStartNode(node) && NodeUtil.isStageNode(node)) {
            handler.onStageStarted(node);
            return;
        }

        if (isEndNode(node)) {
            final var startNode = ((BlockEndNode<?>) node).getStartNode();
            final var timingAction = startNode.getAction(TimingAction.class);
            if (timingAction != null && NodeUtil.isStageNode(startNode)) {
                handler.onStageCompleted(node);
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
