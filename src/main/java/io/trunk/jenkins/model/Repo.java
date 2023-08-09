package io.trunk.jenkins.model;

public class Repo {
    public final String host;
    public final String owner;
    public final String name;

    public Repo(String host, String owner, String name) {
        this.host = host;
        this.owner = owner;
        this.name = name;
    }

    public static Repo githubRepo(String owner, String name) {
        return new Repo("github.com", owner, name);
    }

    public static Repo fromGitUrl(String gitRepoUrl) {
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

        return new Repo(host, owner, name);
    }
}
