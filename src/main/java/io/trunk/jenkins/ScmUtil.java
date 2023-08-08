package io.trunk.jenkins;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import io.trunk.jenkins.model.Repo;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ScmUtil {

    private static final Logger LOG = Logger.getLogger(ScmUtil.class.getName());

    public static List<Repo> getRepos(@NonNull WorkflowRun run, TaskListener listener) {
        run.getSCMs().forEach(scm -> {
            if (scm instanceof GitSCM) {
                final var git = (GitSCM) scm;
                git.getUserRemoteConfigs().forEach(remote -> {
                    LOG.info(String.format("Found git remote: %s", remote.getUrl()));
                    // TODO(zaycev): convert to repo record.
                });
            }
        });
        return Collections.singletonList(new Repo("Github", "trunk-io", "trunk"));
    }


}
