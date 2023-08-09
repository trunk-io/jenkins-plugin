package io.trunk.jenkins.utils;

import io.trunk.jenkins.model.Repo;

import java.util.List;
import java.util.logging.Logger;



/**
 * Utility type which resolves and appends common fields to all events on same pipeline run.
 */
public class TagsUtility {

    private static final Logger LOG = Logger.getLogger(TagsUtility.class.getName());

    public final String runId;
    public final List<Repo> repos;

    public TagsUtility(String runId, List<Repo> repos) {
        this.runId = runId;
        this.repos = repos;
    }


}
