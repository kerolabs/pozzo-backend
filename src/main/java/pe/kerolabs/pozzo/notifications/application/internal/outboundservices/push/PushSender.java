package pe.kerolabs.pozzo.notifications.application.internal.outboundservices.push;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Outbound port towards the push notification provider.
 */
public interface PushSender {

    /**
     * Sends a notification to one device.
     *
     * @param pushToken      the token of the device
     * @param notificationId the notification, so the application can open it
     * @param title          the title
     * @param body           the text
     * @param deepLink       the screen it opens, or null
     * @return what happened with the delivery
     */
    PushOutcome send(String pushToken, UUID notificationId, String title, String body, @Nullable String deepLink);

    /**
     * Result of a delivery: delivered, the token is no longer valid, or a failure worth retrying.
     */
    enum PushOutcome {
        DELIVERED,
        INVALID_TOKEN,
        FAILED
    }
}
