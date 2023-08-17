package io.trunk.jenkins.model.event;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface ActivityPayloadForm {
    @Nullable
    List<ActivityMetricForm> metrics();

    @Nullable
    List<ActivityIntegerTagForm> tagsInt64();

    @Nullable
    List<ActivityStringTagForm> tagsString();

    @Nullable
    List<ActivityTimestampForm> timestamps();

    static String getTitle(ActivityPayloadForm payload) {
        final var tags = payload.tagsString();
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
}
