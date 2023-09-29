package io.jenkins.plugins.trunk.utils;

import io.jenkins.plugins.trunk.Configuration;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Collections;

public class SecretsUtil {

    public static @CheckForNull String getTrunkToken() {
        final var secretId = Configuration.get().tokenSecretName;

        final var credentials = CredentialsProvider.lookupCredentials(
                StringCredentials.class,
                Jenkins.get(),
                null,
                Collections.<DomainRequirement>emptyList()
        );
        final var cred = CredentialsMatchers.firstOrNull(credentials, CredentialsMatchers.withId(secretId));
        if (cred != null) {
            return cred.getSecret().getPlainText();
        }
        return null;
    }

}
