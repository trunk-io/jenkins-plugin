package io.trunk.jenkins.model.event;

import io.trunk.jenkins.model.Timestamp;

public class ActivityEventForm {
    public String id;
    public String chainId;
    public String parentId;
    public ActivityKind kind;
    public String origin;
    public Timestamp createdAt;
    public Timestamp finishedAt;
    public ActivityConclusion conclusion;
    public ActivityPayloadForm payload;
    public FactForm fact;
}
