package io.trunk.jenkins.model;

/**
 * POJO for repo information.
 */
public class Repo {
    public final String provider;
    public final String owner;
    public final String name;

    public Repo(String provider, String owner, String name) {
        this.provider = provider;
        this.owner = owner;
        this.name = name;
    }
}
