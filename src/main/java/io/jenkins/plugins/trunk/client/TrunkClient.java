package io.jenkins.plugins.trunk.client;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.plugins.trunk.TrunkLog;
import io.jenkins.plugins.trunk.model.Metadata;
import io.jenkins.plugins.trunk.model.Repo;
import io.jenkins.plugins.trunk.model.event.ActivityPayloadForm;
import io.jenkins.plugins.trunk.model.service.TrackEventsRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Simple wrapper around OkHTTP client to make requests to Trunk API.
 */
public class TrunkClient {

    private static final String SOURCE = "jenkins-plugin";
    private static final String TRACK_EVENTS_PATH = "/v1/metrics/trackEvents";
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
        for (final var event : req.events()) {
            TrunkLog.info(String.format("Tracking event %s for repo %s",
                    ActivityPayloadForm.getTitle(event.payload()),
                    Repo.getFullName(req.repo())
            ));
        }

        final var body = this.gson.toJson(req);
        final var httpReq = new Request.Builder()
                .url(rpcUrl)
                .header("x-api-token", md.token())
                .header("x-source", SOURCE)
                .post(RequestBody.create(body, MEDIA_TYPE_JSON))
                .build();
        try (final var resp = this.inner.newCall(httpReq).execute()) {
            if (!resp.isSuccessful()) {
                final var respBody = resp.body();
                if (respBody != null) {
                    LOG.warning(String.format("Failed to upload event to Trunk: %s", respBody.string()));
                } else {
                    LOG.warning(String.format("Failed to upload event to Trunk: %s", resp.message()));
                }
            }
        } catch (IOException e) {
            LOG.warning(String.format("Failed to upload event to Trunk: %s", e.getMessage()));
        }
    }
}
