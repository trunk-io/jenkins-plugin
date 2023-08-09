package io.trunk.jenkins.model.service;

import io.trunk.jenkins.model.Repo;
import io.trunk.jenkins.model.event.ActivityEventForm;

import java.util.List;

public class TrackEventsRequest {
    public Repo repo;
    public List<ActivityEventForm> events;

    public static TrackEventsRequest singleEvent(Repo repo, ActivityEventForm event) {
        final var body = new TrackEventsRequest();
        body.repo = repo;
        body.events = List.of(event);
        return body;
    }

    public static TrackEventsRequest multipleEvents(Repo repo, List<ActivityEventForm> events) {
        final var body = new TrackEventsRequest();
        body.repo = repo;
        body.events = events;
        return body;
    }
}
