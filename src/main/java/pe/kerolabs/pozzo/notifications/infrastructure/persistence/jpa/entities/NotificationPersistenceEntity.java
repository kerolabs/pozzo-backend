package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationKind;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for notifications, indexed by status and moment for the dispatcher.
 */
@Entity
@Table(name = "notifications", schema = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uq_notifications_dedup", columnNames = "dedup_key"),
        indexes = {
                @Index(name = "ix_notifications_due", columnList = "status, scheduled_at"),
                @Index(name = "ix_notifications_period_member", columnList = "period_id, member_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
public class NotificationPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "member_id", nullable = false)
    private UUID accountId;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "period_id")
    private UUID periodId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 10)
    private NotificationKind kind;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "body", nullable = false, length = 240)
    private String body;

    @Column(name = "deep_link", length = 200)
    private String deepLink;

    @Column(name = "dedup_key", nullable = false, length = 120)
    private String dedupKey;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    @Column(name = "attempts", nullable = false)
    private int attempts;
}
