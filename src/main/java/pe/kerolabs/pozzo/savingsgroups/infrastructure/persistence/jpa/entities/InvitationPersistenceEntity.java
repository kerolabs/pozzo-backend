package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities;

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
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for invitations. Codes are unique across every group.
 */
@Entity
@Table(name = "invitations", schema = "savings_groups",
        uniqueConstraints = @UniqueConstraint(name = "uq_invitations_code", columnNames = "code"),
        indexes = @Index(name = "ix_invitations_group_status", columnList = "group_id, status"))
@Getter
@Setter
@NoArgsConstructor
public class InvitationPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "code", nullable = false, length = 8)
    private String code;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private InvitationStatus status;
}
