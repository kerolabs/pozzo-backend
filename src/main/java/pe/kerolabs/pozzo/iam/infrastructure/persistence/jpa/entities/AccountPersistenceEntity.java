package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.AccountStatus;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Theme;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;

/**
 * JPA persistence entity for accounts.
 */
@Entity
@Table(name = "accounts", schema = "identity_access",
        uniqueConstraints = @UniqueConstraint(name = "uq_accounts_phone", columnNames = "phone_number"))
@Getter
@Setter
@NoArgsConstructor
public class AccountPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "photo_url", length = 300)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false, length = 10)
    private Theme theme;

    @Column(name = "terms_accepted_at", nullable = false)
    private Instant termsAcceptedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private AccountStatus status;
}
