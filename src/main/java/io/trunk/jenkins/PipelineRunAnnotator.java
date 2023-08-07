package io.trunk.jenkins;

import io.trunk.jenkins.model.Repo;

import java.util.List;
import java.util.logging.Logger;

public class PipelineRunAnnotator {

    private static final Logger LOG = Logger.getLogger(PipelineRunAnnotator.class.getName());

    public final String runId;
    public final List<Repo> repos;

    public PipelineRunAnnotator(String runId, List<Repo> repos) {
        this.runId = runId;
        this.repos = repos;
    }

}
