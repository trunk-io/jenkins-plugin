package io.jenkins.plugins.trunk.model.service;

import io.jenkins.plugins.trunk.model.Repo;
import io.jenkins.plugins.trunk.model.event.ActivityEventForm;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public interface TrackEventsRequest {
    static TrackEventsRequest forSingleEvent(Repo repo, ActivityEventForm event) {
        return ImmutableTrackEventsRequest.builder()
                .repo(repo)
                .events(List.of(event))
                .build();
    }

    static TrackEventsRequest forMultipleEvents(Repo repo, List<ActivityEventForm> events) {
        return ImmutableTrackEventsRequest.builder()
                .repo(repo)
                .events(events)
                .build();
    }

    Repo repo();

    List<ActivityEventForm> events();
}
