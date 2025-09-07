package com.back2basics.notification;

import com.back2basics.notification.dto.DiscordMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DiscordNotificationService {

    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    public void sendNotification(String message) {
        DiscordMessage discordMessage = new DiscordMessage(message);
        restTemplate.postForEntity(webhookUrl, discordMessage, String.class);
    }
}
