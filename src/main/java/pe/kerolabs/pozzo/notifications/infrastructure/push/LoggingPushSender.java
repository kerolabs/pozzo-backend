package pe.kerolabs.pozzo.notifications.infrastructure.push;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.push.PushSender;

import java.util.UUID;

/**
 * Writes the notifications to the log instead of sending them; for development and for running
 * without a Firebase project. Active unless {@code push.provider=fcm}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "push.provider", havingValue = "log", matchIfMissing = true)
public class LoggingPushSender implements PushSender {

    @Override
    public PushOutcome send(String pushToken, UUID notificationId, String title, String body,
                            @Nullable String deepLink) {
        log.info("Push notification {} to device ...{}: {} | {}", notificationId,
                pushToken.substring(Math.max(0, pushToken.length() - 6)), title, body);
        return PushOutcome.DELIVERED;
    }
}
