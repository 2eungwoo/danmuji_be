package com.back2basics.notification;

import com.back2basics.notification.dto.DiscordMessage;
import com.back2basics.notification.port.out.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationService implements NotificationPort {

    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    @Override
    public void send(String message) {
        try {
            DiscordMessage discordMessage = new DiscordMessage(message);
            restTemplate.postForEntity(webhookUrl, discordMessage, String.class);
        } catch (Exception e) {
            log.error("디스코드 알림 전송에 실패했습니다: {}", e.getMessage());
        }
    }
}
