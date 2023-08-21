package org.trunk.jenkins.listeners;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.jupiter.api.Assertions.*;

public class JenkinsActivityListenerTest {
    private static final String PROJECT_NAME = "test-project";

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testCanTrackPipelineSuccess() throws Exception {
        final var project = jenkins.createFreeStyleProject(PROJECT_NAME);
        final var build = jenkins.buildAndAssertSuccess(project);
    }

    @Test
    public void testCanTrackPipelineFailure() {}

    @Test
    public void testCanTrackStageSuccess() {}

    @Test
    public void testCanTrackStageFailure() {}

}