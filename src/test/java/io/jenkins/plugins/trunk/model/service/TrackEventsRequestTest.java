package io.jenkins.plugins.trunk.model.service;

import com.google.gson.GsonBuilder;
import io.jenkins.plugins.trunk.model.ImmutableTimestamp;
import io.jenkins.plugins.trunk.model.Repo;
import io.jenkins.plugins.trunk.model.event.*;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrackEventsRequestTest {

    @Test
    public void testSerialization() {
        final var gson = (new GsonBuilder()).setPrettyPrinting().create();

        final var req = TrackEventsRequest.forSingleEvent(
                Repo.fromGitUrl("https://github.com/trunk-io/jenkins-plugin.git"),
                ImmutableActivityEventForm.builder()
                        .id("test-event-id")
                        .platform("jenkins")
                        .event("pipeline")
                        .chainId("test-chain-id")
                        .parentId("test-parent-id")
                        .origin("test-origin")
                        .createdAt(ImmutableTimestamp.builder().s(1).n(2).build())
                        .finishedAt(ImmutableTimestamp.builder().s(3).n(4).build())
                        .conclusion(ActivityConclusion.SUCCESS)
                        .payload(ImmutableActivityPayloadForm.builder()
                                .metrics(List.of(
                                        ImmutableActivityMetricForm.builder()
                                                .k("test-metric-key")
                                                .v(1.0)
                                                .build()
                                ))
                                .tags(List.of(
                                        ImmutableActivityTagForm.builder()
                                                .k("test-tag-key")
                                                .v("test-tag-value")
                                                .build()
                                )).build())
                        .fact(ImmutableFactForm.builder()
                                .key("test-fact-key")
                                .name("test-fact-name")
                                .payload(ImmutableFactPayloadForm.builder()
                                        .tags(List.of(
                                                ImmutableActivityTagForm.builder()
                                                        .k("test-fact-tag-key")
                                                        .v("test-fact-tag-value")
                                                        .build()
                                        )).build())
                                .build())
                        .build());


        final var json = gson.toJson(req);

        System.out.println(json);

        assertEquals("{}", json);

    }


}