package io.jenkins.plugins.trunk.utils;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Action;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.BlockStartNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.logging.Logger;

public class NodeUtil {

    private static final Logger LOG = Logger.getLogger(NodeUtil.class.getName());
    private static final String SYNTHETIC_STAGE_TAG = "SYNTHETIC_STAGE";

    public static boolean isStartNode(@NonNull FlowNode node) {
        return node instanceof BlockStartNode;
    }

    public static boolean isEndNode(@NonNull FlowNode node) {
        return node instanceof BlockEndNode;
    }

    public static boolean isSyntheticStageNode(FlowNode node) {
        for (Action action : node.getActions()) {
            if (action instanceof TagsAction) {
                final var tagsAction = (TagsAction) action;
                if (tagsAction.getTagValue(SYNTHETIC_STAGE_TAG) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isStageNode(FlowNode node) {
        if (node == null || isSyntheticStageNode(node)) {
            return false;
        }
        if (node.getAction(StageAction.class) != null) {
            return true;
        }
        return node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null;
    }

    @CheckForNull
    public static WorkflowRun getNodeRun(@NonNull FlowNode node) {
        try {
            return (WorkflowRun) node.getExecution().getOwner().getExecutable();
        } catch (IOException ioe) {
            LOG.warning(String.format("Failed to get run for node %s", node));
            return null;
        }
    }

}
