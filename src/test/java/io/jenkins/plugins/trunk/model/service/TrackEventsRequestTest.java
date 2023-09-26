package io.jenkins.plugins.trunk.model.service;

import com.google.gson.GsonBuilder;
import io.jenkins.plugins.trunk.model.Repo;
import io.jenkins.plugins.trunk.model.event.*;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackEventsRequestTest {

    private final static String EXPECTED_JSON = "{\n" +
            "  \"repo\": {\n" +
            "    \"host\": \"github.com\",\n" +
            "    \"owner\": \"trunk-io\",\n" +
            "    \"name\": \"jenkins-plugin\"\n" +
            "  },\n" +
            "  \"events\": [\n" +
            "    {\n" +
            "      \"id\": \"test-event-id\",\n" +
            "      \"parent\": {\n" +
            "        \"eventId\": \"test-parent-event-id\",\n" +
            "        \"sequenceKey\": \"test-parent-key\"\n" +
            "      },\n" +
            "      \"chainId\": \"test-chain-id\",\n" +
            "      \"origin\": \"test-origin\",\n" +
            "      \"createdAt\": 1,\n" +
            "      \"finishedAt\": 2,\n" +
            "      \"conclusion\": \"SUCCESS\",\n" +
            "      \"payload\": {\n" +
            "        \"metrics\": [\n" +
            "          {\n" +
            "            \"k\": \"test-metric-key\",\n" +
            "            \"v\": 1.0\n" +
            "          }\n" +
            "        ],\n" +
            "        \"tags\": [\n" +
            "          {\n" +
            "            \"k\": \"test-tag-key\",\n" +
            "            \"v\": \"test-tag-value\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"sequence\": {\n" +
            "        \"platform\": \"jenkins\",\n" +
            "        \"kind\": \"pipeline\",\n" +
            "        \"key\": \"test-sequence-key\",\n" +
            "        \"name\": \"test-sequence-name\",\n" +
            "        \"payload\": {\n" +
            "          \"tags\": [\n" +
            "            {\n" +
            "              \"k\": \"test-sequence-tag-key\",\n" +
            "              \"v\": \"test-sequence-tag-value\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    @Test
    public void testSerialization() {
        final var gson = (new GsonBuilder()).setPrettyPrinting().create();

        final var req = TrackEventsRequest.forSingleEvent(
                Repo.fromGitUrl("https://github.com/trunk-io/jenkins-plugin.git"),
                ImmutableActivityEventForm.builder()
                        .id("test-event-id")
                        .chainId("test-chain-id")
                        .parent(ImmutableActivityEventParent
                                .builder()
                                .eventId("test-parent-event-id")
                                .sequenceKey("test-parent-key")
                                .build()
                        )
                        .origin("test-origin")
                        .createdAt(1L)
                        .finishedAt(2L)
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
                        .sequence(ImmutableSequenceForm.builder()
                                .platform("jenkins")
                                .kind("pipeline")
                                .key("test-sequence-key")
                                .name("test-sequence-name")
                                .payload(ImmutableSequencePayloadForm.builder()
                                        .tags(List.of(
                                                ImmutableActivityTagForm.builder()
                                                        .k("test-sequence-tag-key")
                                                        .v("test-sequence-tag-value")
                                                        .build()
                                        )).build())
                                .build())
                        .build());


        final var json = gson.toJson(req);

        System.out.println(json);

        assertEquals(EXPECTED_JSON, json);
    }
}
