package io.trunk.jenkins.client;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.trunk.jenkins.model.Metadata;
import io.trunk.jenkins.model.service.TrackEventsRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.logging.Logger;

public class TrunkClient {
    private static final String SOURCE = "jenkins-plugin";
    private static final String TRACK_EVENTS_PATH = "/v1/metrics/v2/trackEvents";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    private static final Logger LOG = Logger.getLogger(TrunkClient.class.getName());

    private final String rpcUrl;
    private final Gson gson;
    private final OkHttpClient inner;

    public TrunkClient(OkHttpClient inner, Gson gson, String trunkApi) {
        this.gson = gson;
        this.inner = inner;
        this.rpcUrl = String.format("%s%s", trunkApi, TRACK_EVENTS_PATH);
    }

    public void trackEvents(@NonNull TrackEventsRequest req, @NonNull Metadata md) {
        final var body = this.gson.toJson(req);
        final var httpReq = new Request.Builder()
                .url(rpcUrl)
                .header("x-api-token", md.token)
                .header("x-source", SOURCE)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        try (final var resp = this.inner.newCall(httpReq).execute()) {
            if (!resp.isSuccessful()) {
                final var respText = resp.body() != null ? resp.body().string() : "";
                LOG.info(String.format("Successfully uploaded event to Trunk: %s", respText));
            }
        } catch (IOException e) {
            LOG.warning(String.format("Failed to upload event to Trunk: %s", e.getMessage()));
        }

    }

}