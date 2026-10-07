package pe.kerolabs.pozzo.notifications.domain.repositories;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.Notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Notification repository port.
 */
public interface NotificationRepository {

    /**
     * Scheduled notifications whose moment has come, the oldest first.
     */
    List<Notification> findDue(Instant now, int limit);

    /**
     * Scheduled reminders of a member in a period.
     */
    List<Notification> findScheduledByPeriodIdAndAccountId(UUID periodId, UUID accountId);

    boolean existsByDedupKey(String dedupKey);

    /**
     * Notifications already sent to a member, the most recent first.
     */
    List<Notification> findSentByAccountId(UUID accountId, int limit);

    Notification save(Notification notification);
}
