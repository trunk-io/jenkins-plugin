package io.trunk.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Actionable;
import hudson.model.Result;
import hudson.model.Run;
import io.trunk.jenkins.model.Timestamp;
import io.trunk.jenkins.model.event.*;
import io.trunk.jenkins.utils.JobUtil;
import io.trunk.jenkins.utils.NodeUtil;
import io.trunk.jenkins.utils.VersionUtil;
import jenkins.model.Jenkins;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.Blake3;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jfree.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Mapper {

    private static final Logger LOG = Logger.getLogger(Mapper.class.getName());
    private static final String VERSIONED_PLUGIN_ORIGIN = String.format("jenkins-plugin-%s", VersionUtil.getVersion());

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

    private static String getActivityUrl(@NonNull Run<?, ?> run) {
        final var j = Jenkins.get();
        return String.format("%s%s", j.getRootUrl(), run.getUrl());
    }

    private static String getFactUrl(@NonNull Run<?, ?> run) {
        final var j = Jenkins.get();
        return String.format("%s%s", j.getRootUrl(), run.getParent().getUrl());
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
    private static String makeJobRunFactKey(@NonNull Run<?, ?> run) {
        return hashString(run.getParent().getName());
    }

    public static String makeJobRunEventId(@NonNull Run<?, ?> run) {
        return String.format("%s#%s", makeJobRunFactKey(run), run.getNumber());
    }

    private static String makeStageEventId(@NonNull Run<?, ?> run, @NonNull FlowNode node) {
        return String.format("%s#%s", makeStageFactKey(run, node), run.getNumber());
    }

    private static String makeStageParentEventId(@NonNull Run<?, ?> run, @NonNull FlowNode node) {
        return String.format("%s#%d", makeStageParentFactKey(run, node), run.getNumber());
    }

    private static String makeJobRunChainId(@NonNull Run<?, ?> run) {
        return makeJobRunEventId(run);
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

    private static String getFullPath(@NonNull Run<?, ?> run, @NonNull FlowNode node) {
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
    private static String makeStageFactKey(@NonNull Run<?, ?> run, @NonNull FlowNode node) {
        return hashString(getFullPath(run, node));
    }

    private static String makeStageParentFactKey(Run<?, ?> run, FlowNode node) {
        final var parents = getParentStages(node);
        if (parents.isEmpty()) {
            return makeJobRunFactKey(run);
        }
        return makeStageFactKey(run, parents.get(0));
    }

    public static FactForm makePipelineFactForm(@NonNull Run<?, ?> run) {
        return ImmutableFactForm.builder()
                .key(makeJobRunFactKey(run))
                .name(run.getParent().getName())
                .payload(ImmutableFactPayloadForm.builder()
                        .tagsString(Collections.singletonList(ActivityStringTagForm.make("url", getFactUrl(run))))
                        .tagsInt64(Collections.emptyList())
                        .build())
                .build();
    }

    public static ActivityPayloadForm makePipelineActivityEventPayloadForm(@NonNull Run<?, ?> run) {
        final var now = System.currentTimeMillis();
        final var duration = run.getResult() == null ? 0 : now - run.getStartTimeInMillis();
        var type = "unknown";
        if (JobUtil.asWorkflowRun(run) != null) {
            type = "pipeline";
        } else if (JobUtil.asFreestyleBuild(run) != null) {
            type = "freestyle";
        }
        return ImmutableActivityPayloadForm.builder()
                .tagsString(Collections.emptyList())
                .metrics(Collections.singletonList(ActivityMetricForm.make("duration_ms", duration)))
                .tagsInt64(Collections.singletonList(ActivityIntegerTagForm.make("build", run.getNumber())))
                .tagsString(List.of(
                        ActivityStringTagForm.make("title", run.getDisplayName()),
                        ActivityStringTagForm.make("url", getActivityUrl(run)),
                        ActivityStringTagForm.make("t", type)
                )).build();
    }

    private static FactForm makeStageFactForm(@NonNull Run<?, ?> run, @NonNull FlowNode startNode) {
        return ImmutableFactForm.builder()
                .key(makeStageFactKey(run, startNode))
                .name(startNode.getDisplayName())
                .payload(ImmutableFactPayloadForm.builder()
                        .tagsString(Collections.singletonList(ActivityStringTagForm.make("url", getActivityUrl(run))))
                        .tagsInt64(Collections.emptyList())
                        .build())
                .build();
    }

    private static ActivityPayloadForm makeStageActivityEventPayloadForm(
            @NonNull Run<?, ?> run,
            @NonNull FlowNode startNode,
            @Nullable FlowNode endNode) {
        final var duration = endNode == null ? 0 : getTime(endNode) - getTime(startNode);
        return ImmutableActivityPayloadForm.builder()
                .tagsString(Collections.emptyList())
                .metrics(Collections.singletonList(ActivityMetricForm.make("duration_ms", duration)))
                .tagsInt64(Collections.singletonList(ActivityIntegerTagForm.make("build", run.getNumber())))
                .tagsString(List.of(
                        ActivityStringTagForm.make("path", getFullPath(run, startNode)),
                        ActivityStringTagForm.make("title", run.getDisplayName()),
                        ActivityStringTagForm.make("url", getActivityUrl(run)),
                        ActivityStringTagForm.make("t", "stage")
                )).build();
    }

    public static ActivityEventForm newPipelineStartedEvent(@NonNull Run<?, ?> run) {
        return ImmutableActivityEventForm.builder()
                .id(makeJobRunEventId(run))
                .chainId(makeJobRunChainId(run))
                .kind(ActivityKind.JENKINS)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(run.getStartTimeInMillis()))
                .conclusion(ActivityConclusion.UNSPECIFIED)
                .payload(makePipelineActivityEventPayloadForm(run))
                .fact(makePipelineFactForm(run))
                .build();
    }

    public static ActivityEventForm newPipelineCompletedEvent(@NonNull Run<?, ?> run) {
        final var now = System.currentTimeMillis();
        return ImmutableActivityEventForm.builder()
                .id(makeJobRunEventId(run))
                .chainId(makeJobRunChainId(run))
                .kind(ActivityKind.JENKINS)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(run.getStartTimeInMillis()))
                .finishedAt(Timestamp.fromEpochMs(now))
                .conclusion(resultToConclusion(run.getResult()))
                .payload(makePipelineActivityEventPayloadForm(run))
                .fact(makePipelineFactForm(run))
                .build();
    }

    public static ActivityEventForm newStageStartedEvent(
            @NonNull Run<?, ?> run,
            @NonNull FlowNode node) {
        return ImmutableActivityEventForm.builder()
                .id(makeStageEventId(run, node))
                .chainId(makeJobRunChainId(run))
                .parentId(makeStageParentEventId(run, node))
                .kind(ActivityKind.JENKINS)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(getTime(node)))
                .conclusion(ActivityConclusion.UNSPECIFIED)
                .payload(makeStageActivityEventPayloadForm(run, node, null))
                .fact(makeStageFactForm(run, node))
                .build();
    }

    public static ActivityEventForm newStageCompletedEvent(
            @NonNull Run<?, ?> run,
            @NonNull FlowNode startNode,
            @NonNull FlowNode endNode) {
        return ImmutableActivityEventForm.builder()
                .id(makeStageEventId(run, startNode))
                .chainId(makeJobRunChainId(run))
                .parentId(makeStageParentEventId(run, startNode))
                .kind(ActivityKind.JENKINS)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(getTime(startNode)))
                .finishedAt(Timestamp.fromEpochMs(getTime(endNode)))
                .conclusion(endNode.getError() == null ? ActivityConclusion.SUCCESS : ActivityConclusion.FAILURE)
                .payload(makeStageActivityEventPayloadForm(run, startNode, endNode))
                .fact(makeStageFactForm(run, startNode))
                .build();
    }

}
