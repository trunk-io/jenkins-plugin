package io.jenkins.plugins.trunk;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.model.Result;
import hudson.model.Run;
import io.jenkins.plugins.trunk.model.Timestamp;
import io.jenkins.plugins.trunk.model.event.*;
import io.jenkins.plugins.trunk.utils.*;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;


/**
 * Mapper is responsible for mapping Jenkins objects to Trunk objects.
 */
public class Mapper {

    private static final Logger LOG = Logger.getLogger(Mapper.class.getName());
    private static final String VERSIONED_PLUGIN_ORIGIN = String.format("jenkins-plugin-%s", VersionUtil.getVersion());

    public static final String PLATFORM_JENKINS = "jenkins";
    public static final String EVENT_PIPELINE = "pipeline";
    public static final String EVENT_STAGE = "stage";

    public static ActivityEventForm newPipelineStartedEvent(@NonNull Run<?, ?> run) {
        return ImmutableActivityEventForm.builder()
                .id(makeJobRunEventId(run))
                .chainId(makeJobRunChainId(run))
                .platform(PLATFORM_JENKINS)
                .event(EVENT_PIPELINE)
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
                .platform(PLATFORM_JENKINS)
                .event(EVENT_PIPELINE)
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
                .platform(PLATFORM_JENKINS)
                .event(EVENT_STAGE)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(ActionUtil.getStartTimeMillis(node)))
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
                .platform(PLATFORM_JENKINS)
                .event(EVENT_STAGE)
                .origin(VERSIONED_PLUGIN_ORIGIN)
                .createdAt(Timestamp.fromEpochMs(ActionUtil.getStartTimeMillis(startNode)))
                .finishedAt(Timestamp.fromEpochMs(ActionUtil.getStartTimeMillis(endNode)))
                .conclusion(endNode.getError() == null ? ActivityConclusion.SUCCESS : ActivityConclusion.FAILURE)
                .payload(makeStageActivityEventPayloadForm(run, startNode, endNode))
                .fact(makeStageFactForm(run, startNode))
                .build();
    }

    public static FactForm makePipelineFactForm(@NonNull Run<?, ?> run) {
        return ImmutableFactForm.builder()
                .key(makeJobRunFactKey(run))
                .name(run.getParent().getName())
                .payload(ImmutableFactPayloadForm.builder()
                        .tags(Collections.singletonList(ActivityTagForm.make("url", makeFactUrl(run))))
                        .build())
                .build();
    }

    public static ActivityPayloadForm makePipelineActivityEventPayloadForm(@NonNull Run<?, ?> run) {
        final var now = System.currentTimeMillis();
        final var duration = run.getResult() == null ? 0 : now - run.getStartTimeInMillis();
        final var initDuration = ActionUtil.getStartTimeMillis(run) - ActionUtil.getInitTimeMillis(run);
        var type = "unknown";
        if (JobUtil.asWorkflowRun(run) != null) {
            type = "pipeline";
        } else if (JobUtil.asFreestyleBuild(run) != null) {
            type = "freestyle";
        }
        return ImmutableActivityPayloadForm.builder()
                .metrics(List.of(
                        ActivityMetricForm.make("duration_ms", duration),
                        ActivityMetricForm.make("init_ms", initDuration)
                ))
                .timestamps(Collections.singletonList(
                        ActivityTimestampForm.make("init", Timestamp.fromEpochMs(ActionUtil.getInitTimeMillis(run)))
                ))
                .tags(List.of(
                        ActivityTagForm.make("title", run.getDisplayName()),
                        ActivityTagForm.make("url", makeEventUrl(run)),
                        ActivityTagForm.make("type", type),
                        ActivityTagForm.make("build", Integer.toString(run.getNumber()))
                )).build();
    }

    private static FactForm makeStageFactForm(@NonNull Run<?, ?> run, @NonNull FlowNode startNode) {
        return ImmutableFactForm.builder()
                .key(makeStageFactKey(run, startNode))
                .name(startNode.getDisplayName())
                .payload(ImmutableFactPayloadForm.builder()
                        .tags(Collections.singletonList(ActivityTagForm.make("url", makeEventUrl(run))))
                        .build())
                .build();
    }

    private static ActivityPayloadForm makeStageActivityEventPayloadForm(
            @NonNull Run<?, ?> run,
            @NonNull FlowNode startNode,
            @Nullable FlowNode endNode) {
        final var duration = endNode == null ? 0 : ActionUtil.getStartTimeMillis(endNode) - ActionUtil.getStartTimeMillis(startNode);
        return ImmutableActivityPayloadForm.builder()
                .metrics(Collections.singletonList(ActivityMetricForm.make("duration_ms", duration)))
                .tags(List.of(
                        ActivityTagForm.make("path", getFullPath(run, startNode)),
                        ActivityTagForm.make("title", run.getDisplayName()),
                        ActivityTagForm.make("url", makeEventUrl(run)),
                        ActivityTagForm.make("build", Integer.toString(run.getNumber()))
                )).build();
    }

    private static String makeEventUrl(@NonNull Run<?, ?> run) {
        final var j = Jenkins.get();
        return String.format("%s%s", j.getRootUrl(), run.getUrl());
    }

    private static String makeFactUrl(@NonNull Run<?, ?> run) {
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
        LOG.warning("Unknown result: " + result);
        return ActivityConclusion.FAILURE;
    }

    private static String makeJobRunFactKey(@NonNull Run<?, ?> run) {
        return IdGeneratorUtil.hashString(run.getParent().getName());
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
        return IdGeneratorUtil.hashString(getFullPath(run, node));
    }

    private static String makeStageParentFactKey(Run<?, ?> run, FlowNode node) {
        final var parents = getParentStages(node);
        if (parents.isEmpty()) {
            return makeJobRunFactKey(run);
        }
        return makeStageFactKey(run, parents.get(0));
    }

}
