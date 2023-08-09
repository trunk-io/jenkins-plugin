package io.trunk.jenkins.model.event;

import java.util.List;

public class ActivityPayloadForm {
    public List<ActivityMetricForm> metrics;
    public List<ActivityIntegerTagForm> tagsInt64;
    public List<ActivityStringTagForm> tagsString;
    public List<ActivityTimestampForm> timestamps;
}
