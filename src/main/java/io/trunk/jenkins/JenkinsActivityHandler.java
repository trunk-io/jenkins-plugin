package io.trunk.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.trunk.jenkins.model.Repo;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.BlockEndNode;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class JenkinsActivityHandler {

    private static final Logger LOG = Logger.getLogger(JenkinsActivityHandler.class.getName());
    private final ConcurrentHashMap<String, InFlightRun> stagingBuffer = new ConcurrentHashMap<>();

    public static class InFlightRun {
        final WorkflowRun run;
        final ArrayList<FlowNode> nodes;
        final EventAnnotator annotator;
        final long startedAtEpochMs = System.currentTimeMillis();

        public InFlightRun(WorkflowRun run, ArrayList<FlowNode> nodes, EventAnnotator annotator) {
            this.run = run;
            this.nodes = nodes;
            this.annotator = annotator;
        }
    }

    public void onPipelineStarted(@NonNull WorkflowRun run, List<Repo> repos) {
        run.addAction(new TimingAction());
        final var inFlightRun = new InFlightRun(run, new ArrayList<>(), new EventAnnotator(run.getId(), repos));
        stagingBuffer.put(run.getId(), inFlightRun);
        LOG.info(String.format("TODO: Pipeline started %s", run.getDisplayName()));
    }

    public void onPipelineCompleted(@NonNull WorkflowRun run) {
        final var action = run.getAction(TimingAction.class);
        if (action != null) {

            // Upload events.
            final var inFlightRun = stagingBuffer.get(run.getId());
            final var config = Configuration.get();
            final var token = config.getToken();

            final var duration = System.currentTimeMillis() - inFlightRun.startedAtEpochMs;
            LOG.info(String.format("TODO: Pipeline completed %s and took %dms", run.getDisplayName(), duration));

            if (!inFlightRun.annotator.repos.isEmpty()) {
                inFlightRun.annotator.repos.forEach((repo -> {
                    LOG.info(String.format("TODO Uploading %d events for repo %s, run %s using token %s",
                            inFlightRun.nodes.size(),
                            repo.name,
                            run.getId(),
                            token
                    ));
                }));
            }

            stagingBuffer.remove(run.getId());
        }
    }

    public void onStageStarted(@NonNull FlowNode node) {
        try {
            final var run = (WorkflowRun) node.getExecution().getOwner().getExecutable();
            node.addAction(new TimingAction());
            stagingBuffer.get(run.getId()).nodes.add(node);
            LOG.info(String.format("Stage started %s", node.getDisplayName()));
        } catch (IOException e) {
            LOG.warning(e.getMessage());
        }
    }

    public void onStageCompleted(@NonNull FlowNode node) {
        try {
            final var run = (WorkflowRun) node.getExecution().getOwner().getExecutable();
            node.addAction(new TimingAction());
            stagingBuffer.get(run.getId()).nodes.add(node);
            final var startNode = ((BlockEndNode<?>) node).getStartNode();
            final var duration = System.currentTimeMillis() - startNode.getAction(TimingAction.class).getStartTime();
            LOG.info(String.format("Stage completed %s and took %d ms", node.getDisplayName(), duration));
        } catch (IOException e) {
            LOG.warning(e.getMessage());
        }
    }


}
