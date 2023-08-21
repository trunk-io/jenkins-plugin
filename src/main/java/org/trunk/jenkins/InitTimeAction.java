package org.trunk.jenkins;

import hudson.model.Action;

public class InitTimeAction implements Action {

    private final long initTime = System.currentTimeMillis();

    public long getInitTime() {
        return initTime;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Init time";
    }

    @Override
    public String getUrlName() {
        return null;
    }
}
