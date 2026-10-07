package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for the codes sent to a backup email to recover an account.
 */
@Entity
@Table(name = "recovery_codes", schema = "identity_access",
        indexes = @Index(name = "ix_recovery_codes_email_status", columnList = "email, status"))
@Getter
@Setter
@NoArgsConstructor
public class RecoveryCodePersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 120)
    private String codeHash;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private VerificationStatus status;
}
