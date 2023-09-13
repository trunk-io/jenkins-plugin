package io.jenkins.plugins.trunk.model.event;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface ActivityPayloadForm {
    static String getTitle(ActivityPayloadForm payload) {
        final var tags = payload.tags();
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        for (final var tag : tags) {
            if (tag.k().equals("title")) {
                return tag.v();
            }
        }
        return "";
    }

    @Nullable
    List<ActivityMetricForm> metrics();

    @Nullable
    List<ActivityTagForm> tags();

    @Nullable
    List<ActivityTimestampForm> timestamps();
}
