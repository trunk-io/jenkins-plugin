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

    public static Repo GithubRepo(String owner, String name) {
        return new Repo("github.com", owner, name);
    }
}
