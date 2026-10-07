package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for sessions. The token itself is never stored, only its hash.
 */
@Entity
@Table(name = "sessions", schema = "identity_access",
        uniqueConstraints = @UniqueConstraint(name = "uq_sessions_token", columnNames = "token_hash"),
        indexes = @Index(name = "ix_sessions_account", columnList = "account_id, revoked_at"))
@Getter
@Setter
@NoArgsConstructor
public class SessionPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "token_hash", nullable = false, length = 120)
    private String tokenHash;

    @Column(name = "device_label", length = 80)
    private String deviceLabel;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}
