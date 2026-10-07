package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA persistence entity for what a member owes in a period.
 */
@Entity
@Table(name = "expected_contributions", schema = "contributions",
        uniqueConstraints = @UniqueConstraint(name = "uq_expected_member", columnNames = {"period_id", "membership_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ExpectedContributionPersistenceEntity extends AbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private PeriodPersistenceEntity period;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ExpectedStatus status;

    @Column(name = "settled_by_contribution")
    private UUID settledByContribution;
}
