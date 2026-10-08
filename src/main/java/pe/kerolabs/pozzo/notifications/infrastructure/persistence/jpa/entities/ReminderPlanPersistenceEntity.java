package pe.kerolabs.pozzo.notifications.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for reminder plans; the offsets are stored as a comma-separated list, e.g. "3,1,0".
 */
@Entity
@Table(name = "reminder_plans", schema = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uq_reminder_plans_group", columnNames = "group_id"))
@Getter
@Setter
@NoArgsConstructor
public class ReminderPlanPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "offsets_in_days", nullable = false, length = 30)
    private String offsetsInDays;

    @Column(name = "send_hour", nullable = false)
    private int sendHour;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
