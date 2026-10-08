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
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.Platform;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for devices. A push token belongs to one device only.
 */
@Entity
@Table(name = "devices", schema = "notifications",
        uniqueConstraints = @UniqueConstraint(name = "uq_devices_token", columnNames = "push_token"),
        indexes = @Index(name = "ix_devices_member", columnList = "member_id, active"))
@Getter
@Setter
@NoArgsConstructor
public class DevicePersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "member_id", nullable = false)
    private UUID accountId;

    @Column(name = "push_token", nullable = false, length = 255)
    private String pushToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 10)
    private Platform platform;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "active", nullable = false)
    private boolean active;
}
