package io.jenkins.plugins.trunk.utils;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Collections;

public class SecretsUtil {

    private static final String TRUNK_SECRET_ID = "TRUNK_TOKEN";

    public static @CheckForNull String getTrunkToken() {
        final var credentials = CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                Jenkins.get(),
                null,
                Collections.<DomainRequirement>emptyList()
        );
        final var cred = CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(TRUNK_SECRET_ID));
        if (cred != null) {
            return cred.getSecret().getPlainText();
        }
        return null;
    }

}
