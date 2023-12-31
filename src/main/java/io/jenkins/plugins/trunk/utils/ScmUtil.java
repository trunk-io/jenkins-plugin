package io.jenkins.plugins.trunk.utils;

import com.coravy.hudson.plugins.github.GithubProjectProperty;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import io.jenkins.plugins.trunk.model.Repo;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ScmUtil {

    private static final Logger LOG = Logger.getLogger(ScmUtil.class.getName());

    public static List<Repo> getRepos(@NonNull Run<?, ?> run, TaskListener listener) {

        final var buildConsolePrinter = listener.getLogger();
        final var repos = new ArrayList<Repo>();

        buildConsolePrinter.println("Scanning for git remotes");

        final var job = run.getParent();
        final var github = job.getProperty(GithubProjectProperty.class);
        if (github != null) {
            final var url = github.getProjectUrl().toString();
            LOG.info(String.format("Found github remote: %s", url));
            listener.getLogger().printf("Found github remote: %s%n", url);
            repos.add(Repo.fromGitUrl(url));
        }

        if (repos.isEmpty()) {
            final var workflowRun = JobUtil.asWorkflowRun(run);
            if (workflowRun != null) {
                workflowRun.getSCMs().forEach(scm -> {
                    if (scm instanceof GitSCM) {
                        final var git = (GitSCM) scm;
                        git.getUserRemoteConfigs().forEach(remote -> {
                            LOG.info(String.format("Found git remote: %s", remote.getUrl()));
                            listener.getLogger().printf("Found git remote: %s%n", remote.getUrl());
                            final var url = remote.getUrl();
                            if (StringUtils.isNotEmpty(url)) {
                                repos.add(Repo.fromGitUrl(url));
                            }
                        });
                    }
                });
            }
        }

        if (repos.isEmpty()) {
            LOG.warning("No git remotes found");
            listener.getLogger().println("Trunk plugin requires a git remote to be configured");
        }

        return repos;
    }

}
