package io.trunk.jenkins;

import hudson.model.Action;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TagsAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.logging.Logger;

public class NodeUtil {

    private static final Logger LOG = Logger.getLogger(NodeUtil.class.getName());
    private static final String SYNTHETIC_STAGE_TAG = "SYNTHETIC_STAGE";

    public static boolean isSyntheticStage(FlowNode node) {
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
        if (node == null || isSyntheticStage(node)) {
            return false;
        }
        if (node.getAction(StageAction.class) != null) {
            return true;
        }
        if (node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null) {
            return true;
        }
        return false;
    }


}
