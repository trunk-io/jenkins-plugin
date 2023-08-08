package io.trunk.jenkins;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Type providing global plugin configuration.
 */
@Extension
public class Configuration extends GlobalConfiguration {
    public String token;

    public Configuration() {
        load();
    }

    public static Configuration get() {
        return GlobalConfiguration.all().get(Configuration.class);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        req.bindJSON(this, json);
        save();
        return true;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}