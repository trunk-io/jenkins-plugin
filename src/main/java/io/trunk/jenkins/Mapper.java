package io.trunk.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Actionable;
import hudson.model.Result;
import io.trunk.jenkins.model.Timestamp;
import io.trunk.jenkins.model.event.*;
import io.trunk.jenkins.utils.NodeUtil;
import io.trunk.jenkins.utils.VersionUtil;
import jenkins.model.Jenkins;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Blake3;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jfree.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Mapper {

    private static final Logger LOG = Logger.getLogger(Mapper.class.getName());
    private static final String ORIGIN = String.format("jenkins-plugin-%s", VersionUtil.getVersion());

    private static String hashString(String name) {
        final var h = Blake3.initHash();
        final byte[] buf = new byte[16]; // 128 bit
        h.update(name.getBytes(StandardCharsets.UTF_8));
        h.doFinalize(buf);
        return Hex.encodeHexString(buf);
    }

    private static long getTime(Actionable node) {
        final var timingAction = node.getAction(TimingAction.class);
        if (timingAction != null) {
            return timingAction.getStartTime();
        }
        throw new RuntimeException("No timing action found");
    }

    private static ActivityConclusion resultToConclusion(Result result) {
        if (Result.SUCCESS.equals(result)) {
            return ActivityConclusion.SUCCESS;
        }
        if (Result.FAILURE.equals(result)) {
            return ActivityConclusion.FAILURE;
        }
        if (Result.ABORTED.equals(result)) {
            return ActivityConclusion.CANCELLED;
        }
        if (Result.UNSTABLE.equals(result)) {
            return ActivityConclusion.FAILURE;
        }
        if (Result.NOT_BUILT.equals(result)) {
            return ActivityConclusion.CANCELLED;
        }
        Log.warn("Unknown result: " + result);
        return ActivityConclusion.FAILURE;
    }

    /**
     * Pipeline key is a hash of the pipeline name.
     */
    private static String makePipelineFactKey(@NonNull WorkflowRun run) {
        return hashString(run.getParent().getName());
    }

    /**
     * Returns a list of all parent stages of the given node.
     */
    private static List<FlowNode> getParentStages(@NonNull FlowNode node) {
        final var stages = new ArrayList<FlowNode>();
        for (var block : node.iterateEnclosingBlocks()) {
            if (NodeUtil.isStageNode(block)) {
                stages.add(block);
            }
        }
        return stages;
    }

    private static String getFullPath(@NonNull WorkflowRun run, @NonNull FlowNode node) {
        final var names = new ArrayList<String>();
        names.add(node.getDisplayName());
        getParentStages(node).forEach(parent -> names.add(parent.getDisplayName()));
        names.add(run.getParent().getDisplayName());
        Collections.reverse(names);
        return String.join("/", names);
    }

    /**
     * Stage id is a hash of stage name with all of its parent stage names.
     * <pre>
     * Pipeline_1
     *   Stage_1
     *     Stage_1.1
     * </pre>
     * Will become hash("Pipeline_1/Stage_1/Stage_1.1").
     */
    private static String makeStageFactKey(@NonNull WorkflowRun run, @NonNull FlowNode node) {
        return hashString(getFullPath(run, node));
    }

    private static String makeStageParentFactKey(WorkflowRun run, FlowNode node) {
        final var parents = getParentStages(node);
        if (parents.size() > 1) {
            return makeStageFactKey(run, parents.get(0));
        }
        return makePipelineFactKey(run);
    }

    public static FactForm makePipelineFactForm(@NonNull WorkflowRun run) {
        final var url = String.format("%s%s", Jenkins.get().getRootUrl(), run.getParent().getUrl());
        final var fact = new FactForm();
        fact.key = makePipelineFactKey(run);
        fact.name = run.getParent().getName();
        fact.payload = new FactPayloadForm();
        fact.payload.tagsString = Collections.singletonList(new ActivityStringTagForm("url", url));
        fact.payload.tagsInt64 = Collections.emptyList();
        return fact;
    }

    public static ActivityPayloadForm makePipelineActivityEventPayloadForm(@NonNull WorkflowRun run) {
        final var payload = new ActivityPayloadForm();
        final var now = System.currentTimeMillis();
        final var duration = run.getResult() == null ? 0 : now - run.getStartTimeInMillis();
        payload.tagsString = Collections.emptyList();
        payload.metrics = Collections.singletonList(new ActivityMetricForm("duration_ms", duration));
        payload.tagsInt64 = Collections.singletonList(new ActivityIntegerTagForm("build", run.getNumber()));
        payload.tagsString = List.of(
                new ActivityStringTagForm("title", run.getDisplayName()),
                new ActivityStringTagForm("url", run.getUrl())
        );
        return payload;
    }

    private static FactForm makeStageFactForm(@NonNull WorkflowRun run, @NonNull FlowNode startNode) {
        final var url = String.format("%s%s", Jenkins.get().getRootUrl(), run.getParent().getUrl());
        final var fact = new FactForm();
        fact.key = makeStageFactKey(run, startNode);
        fact.name = startNode.getDisplayName();
        fact.payload = new FactPayloadForm();
        fact.payload.tagsString = List.of(
                new ActivityStringTagForm("url", url)
        );
        fact.payload.tagsInt64 = Collections.emptyList();
        return fact;
    }

    private static ActivityPayloadForm makeStageActivityEventPayloadForm(@NonNull WorkflowRun run, @NonNull FlowNode startNode, @Nullable FlowNode endNode) {
        final var payload = new ActivityPayloadForm();
        final var duration = endNode == null ? 0 : getTime(startNode) - getTime(endNode);
        payload.tagsString = Collections.emptyList();
        payload.metrics = Collections.singletonList(new ActivityMetricForm("duration_ms", duration));
        payload.tagsInt64 = Collections.singletonList(new ActivityIntegerTagForm("build", run.getNumber()));
        payload.tagsString = List.of(
                new ActivityStringTagForm("path", getFullPath(run, startNode)),
                new ActivityStringTagForm("title", startNode.getDisplayName())
        );
        return payload;
    }


    public static ActivityEventForm newPipelineStartedEvent(@NonNull WorkflowRun run) {
        final var event = new ActivityEventForm();
        event.id = String.format("%s#%d", makePipelineFactKey(run), run.getNumber());
        event.chainId = event.id;
        event.parentId = null; // Pipelines are root events.
        event.kind = ActivityKind.JENKINS;
        event.origin = ORIGIN;
        event.createdAt = Timestamp.fromEpochMs(run.getStartTimeInMillis());
        event.finishedAt = null;
        event.conclusion = ActivityConclusion.UNSPECIFIED;
        event.payload = makePipelineActivityEventPayloadForm(run);
        event.fact = makePipelineFactForm(run);
        return event;
    }

    public static ActivityEventForm newPipelineCompletedEvent(@NonNull WorkflowRun run) {
        final var event = new ActivityEventForm();
        final var now = System.currentTimeMillis();
        event.id = String.format("%s#%d", makePipelineFactKey(run), run.getNumber());
        event.chainId = event.id;
        event.parentId = null; // Pipelines are root events.
        event.kind = ActivityKind.JENKINS;
        event.origin = ORIGIN;
        event.createdAt = Timestamp.fromEpochMs(run.getStartTimeInMillis());
        event.finishedAt = Timestamp.fromEpochMs(now);
        event.conclusion = resultToConclusion(run.getResult());
        event.payload = makePipelineActivityEventPayloadForm(run);
        event.fact = makePipelineFactForm(run);
        return event;
    }

    public static ActivityEventForm newStageStartedEvent(@NonNull WorkflowRun run, @NonNull FlowNode node) {
        final var event = new ActivityEventForm();
        event.id = String.format("%s#%d", makeStageFactKey(run, node), run.getNumber());
        event.chainId = makePipelineFactKey(run);
        event.parentId = String.format("%s#%d", makeStageParentFactKey(run, node), run.getNumber());
        event.kind = ActivityKind.JENKINS;
        event.origin = ORIGIN;
        event.createdAt = Timestamp.fromEpochMs(getTime(node));
        event.finishedAt = null;
        event.conclusion = ActivityConclusion.UNSPECIFIED;
        event.payload = makeStageActivityEventPayloadForm(run, node, null);
        event.fact = makeStageFactForm(run, node);
        return event;
    }

    public static ActivityEventForm newStageCompletedEvent(@NonNull WorkflowRun run, @NonNull FlowNode startNode, @NonNull FlowNode endNode) {
        final var event = new ActivityEventForm();
        event.id = String.format("%s#%d", makeStageFactKey(run, startNode), run.getNumber());
        event.chainId = makePipelineFactKey(run);
        event.parentId = String.format("%s#%d", makeStageParentFactKey(run, startNode), run.getNumber());
        event.kind = ActivityKind.JENKINS;
        event.origin = ORIGIN;
        event.createdAt = Timestamp.fromEpochMs(getTime(startNode));
        event.finishedAt = Timestamp.fromEpochMs(getTime(endNode));
        event.conclusion = endNode.getError() == null ? ActivityConclusion.SUCCESS : ActivityConclusion.FAILURE;
        event.payload = makeStageActivityEventPayloadForm(run, startNode, endNode);
        event.fact = makeStageFactForm(run, startNode);
        return event;
    }

}
