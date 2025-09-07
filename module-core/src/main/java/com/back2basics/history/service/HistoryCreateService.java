package com.back2basics.history.service;

import com.back2basics.history.model.History;
import com.back2basics.history.port.in.command.HistoryCreateCommand;
import com.back2basics.history.port.out.HistoryCreatePort;
import com.back2basics.notification.DiscordNotificationService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryCreateService {

    private final HistoryCreatePort historyCreatePort;
    private final DiscordNotificationService discordNotificationService;

    @Async
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000),
        retryFor = RuntimeException.class
    )
    public void create(HistoryCreateCommand command) {
        History history = History.create(
            command.historyType(),
            command.domainType(),
            command.domainId(),
            String.valueOf(command.changerId()),
            command.changerName(),
            command.changerUsername(),
            command.changerRole(),
            Map.of("before", command.before()),
            Map.of("after", command.after()),
            LocalDateTime.now(),
            command.message()
        );
        historyCreatePort.save(history);
    }

    @Async
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000),
        retryFor = RuntimeException.class
    )
    public void create(HistoryCreateCommand command, Boolean isDeleted) {
        History history = History.create(
            command.historyType(),
            command.domainType(),
            command.domainId(),
            String.valueOf(command.changerId()),
            String.valueOf(command.changerName()),
            command.changerUsername(),
            command.changerRole(),
            Map.of("before", command.before()),
            Map.of("isDeleted", isDeleted),
            LocalDateTime.now(),
            command.message()
        );
        historyCreatePort.save(history);
    }

    @Recover
    public void recover(RuntimeException e, HistoryCreateCommand command) {
        String errorMessage = String.format(
            "## ⚠️ 이력 저장 실패 ⚠️\n" +
                "### 재시도에 최종 실패하여 이력 저장이 유실되었습니다.\n" +
                "- **에러**: `%s`\n" +
                "- **커맨드**: `%s`",
            e.getMessage(), command.toString()
        );
        discordNotificationService.sendNotification(errorMessage);
    }

    @Recover
    public void recover(RuntimeException e, HistoryCreateCommand command, Boolean isDeleted) {
        String errorMessage = String.format(
            "## ⚠️ 이력 저장 실패 ⚠️\n" +
                "### 재시도에 최종 실패하여 이력 저장이 유실되었습니다.\n" +
                "- **에러**: `%s`\n" +
                "- **커맨드**: `%s`\n" +
                "- **삭제 여부**: `%b`",
            e.getMessage(), command.toString(), isDeleted
        );
        discordNotificationService.sendNotification(errorMessage);
    }
}
