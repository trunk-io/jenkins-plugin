package io.trunk.jenkins.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.immutables.value.Value;

@Value.Immutable
public interface Repo {
    String host();
    String owner();
    String name();

    static Repo github(String owner, String name) {
        return ImmutableRepo.builder()
                .host("github.com")
                .owner(owner)
                .name(name)
                .build();
    }

    static String getFullName(@NonNull Repo repo) {
        return repo.owner() + "/" + repo.name();
    }

    static Repo fromGitUrl(String gitRepoUrl) {
        String[] parts = gitRepoUrl.split("/");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid repo URL: " + gitRepoUrl);
        }

        // https://github.com will be:
        // parts[0] = "https:"
        // parts[1] = ""
        // parts[2] = "github.com"
        // parts[3] = "owner"
        // parts[4] = "name.git"

        final var host = parts[2];
        final var owner = parts[3];
        final var name = parts[4].replace(".git", "");

        return ImmutableRepo.builder()
                .host(host)
                .owner(owner)
                .name(name)
                .build();
    }
}
