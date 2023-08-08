package io.trunk.jenkins.client;

import okhttp3.Request;

/**
 * HTTP client for uploading activity events to Trunk.
 */
public class MetricsClient {

    public static void createEvents() {

        Request req = new Request.Builder()
                .url("https://api.trunk.io")
                .build();

        // TODO: add this.
    }

}
