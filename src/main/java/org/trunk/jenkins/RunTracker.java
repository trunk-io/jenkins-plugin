package org.trunk.jenkins;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import hudson.model.Run;

import java.util.concurrent.TimeUnit;

public class RunTracker {

    // List of started traces.
    // If we received event before pipeline start, we will not send it to Trunk.
    private final Cache<String, Boolean> traceStarted = CacheBuilder.newBuilder()
            .maximumSize(100_000)
            .expireAfterWrite(7, TimeUnit.DAYS)
            .build();

    public void trackRun(Run<?, ?> run) {
        traceStarted.put(Mapper.makeJobRunEventId(run), Boolean.TRUE);
    }

    public boolean isRunTracked(Run<?, ?> run) {
        return traceStarted.getIfPresent(Mapper.makeJobRunEventId(run)) != null;
    }

}
