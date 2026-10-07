package pe.kerolabs.pozzo.notifications.infrastructure.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DispatchDueNotificationsCommand;

/**
 * Sends the notifications whose moment arrived, every minute by default
 * ({@code notifications.dispatch-interval-ms}).
 */
@Slf4j
@Component
public class NotificationDispatchScheduler {

    private static final int BATCH_SIZE = 200;

    private final NotificationCommandService notificationCommandService;

    public NotificationDispatchScheduler(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @Scheduled(fixedDelayString = "${notifications.dispatch-interval-ms:60000}",
            initialDelayString = "${notifications.dispatch-interval-ms:60000}")
    public void dispatch() {
        try {
            var sent = notificationCommandService.handle(new DispatchDueNotificationsCommand(BATCH_SIZE)).getOrElse(0);
            if (sent > 0) {
                log.info("Dispatched {} notifications", sent);
            }
        } catch (RuntimeException e) {
            log.error("Notification dispatch failed", e);
        }
    }
}
