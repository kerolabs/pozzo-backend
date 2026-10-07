package pe.kerolabs.pozzo.notifications.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationContent;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationKind;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationStatus;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Notification aggregate root: a reminder or an alert for a member, with the moment it goes out and
 * a deduplication key, so the same fact never produces two notifications.
 */
@Getter
public class Notification extends AbstractDomainAggregateRoot<Notification> {

    public static final int MAX_ATTEMPTS = 3;

    private UUID id;
    private UUID accountId;
    private UUID groupId;
    private @Nullable UUID periodId;
    private NotificationKind kind;
    private NotificationContent content;
    private String dedupKey;
    private Instant scheduledAt;
    private @Nullable Instant sentAt;
    private NotificationStatus status;
    private int attempts;

    public Notification() {
    }

    /**
     * A reminder that goes out at a given moment unless it is cancelled before.
     */
    public static Notification reminder(UUID accountId, UUID groupId, UUID periodId, NotificationContent content,
                                        String dedupKey, Instant scheduledAt) {
        return create(accountId, groupId, periodId, NotificationKind.REMINDER, content, dedupKey, scheduledAt);
    }

    /**
     * An alert that goes out right away.
     */
    public static Notification alert(UUID accountId, UUID groupId, @Nullable UUID periodId,
                                     NotificationContent content, String dedupKey, Instant now) {
        return create(accountId, groupId, periodId, NotificationKind.ALERT, content, dedupKey, now);
    }

    public boolean isDue(Instant now) {
        return status == NotificationStatus.SCHEDULED && !now.isBefore(scheduledAt);
    }

    public void markSent(Instant now) {
        this.status = NotificationStatus.SENT;
        this.sentAt = now;
        this.attempts++;
    }

    /**
     * Records a failed attempt; after the third one the notification is given up.
     */
    public void markFailed() {
        this.attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            this.status = NotificationStatus.FAILED;
        }
    }

    /**
     * Cancels a scheduled notification, e.g. a reminder once the member paid.
     */
    public void cancel() {
        if (status == NotificationStatus.SCHEDULED) {
            this.status = NotificationStatus.CANCELLED;
        }
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID accountId, UUID groupId, @Nullable UUID periodId, NotificationKind kind,
                             NotificationContent content, String dedupKey, Instant scheduledAt,
                             @Nullable Instant sentAt, NotificationStatus status, int attempts) {
        this.id = id;
        this.accountId = accountId;
        this.groupId = groupId;
        this.periodId = periodId;
        this.kind = kind;
        this.content = content;
        this.dedupKey = dedupKey;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.status = status;
        this.attempts = attempts;
    }

    private static Notification create(UUID accountId, UUID groupId, @Nullable UUID periodId, NotificationKind kind,
                                       NotificationContent content, String dedupKey, Instant scheduledAt) {
        if (dedupKey == null || dedupKey.isBlank() || dedupKey.length() > 120) {
            throw new IllegalArgumentException("The deduplication key is required and cannot exceed 120 characters");
        }
        var notification = new Notification();
        notification.id = UUID.randomUUID();
        notification.accountId = accountId;
        notification.groupId = groupId;
        notification.periodId = periodId;
        notification.kind = kind;
        notification.content = content;
        notification.dedupKey = dedupKey;
        notification.scheduledAt = scheduledAt;
        notification.status = NotificationStatus.SCHEDULED;
        notification.attempts = 0;
        return notification;
    }
}
