package com.back2basics.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscordMessage(
    @JsonProperty("content") String content
) {
}