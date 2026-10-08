package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReceiptSource;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.InconsistencyEmbeddable;
import pe.kerolabs.pozzo.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA persistence entity for contributions. The receipt fields live in columns of the same row; a
 * receipt can be used only once per cycle, enforced by the unique operation number.
 */
@Entity
@Table(name = "contributions", schema = "contributions",
        uniqueConstraints = @UniqueConstraint(name = "uq_contributions_operation",
                columnNames = {"cycle_id", "receipt_operation_number"}),
        indexes = @Index(name = "ix_contributions_period_member", columnList = "period_id, membership_id"))
@Getter
@Setter
@NoArgsConstructor
public class ContributionPersistenceEntity extends AbstractPersistenceEntity {

    @Column(name = "cycle_id", nullable = false)
    private UUID cycleId;

    @Column(name = "period_id", nullable = false)
    private UUID periodId;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 10)
    private ContributionMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ContributionStatus status;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "registered_by_account_id", nullable = false)
    private UUID registeredByAccountId;

    @Column(name = "receipt_operation_number", length = 40)
    private String receiptOperationNumber;

    @Column(name = "receipt_image_path", length = 200)
    private String receiptImagePath;

    @Column(name = "receipt_payer", length = 120)
    private String receiptPayer;

    @Column(name = "receipt_payee", length = 120)
    private String receiptPayee;

    @Column(name = "receipt_amount", precision = 12, scale = 2)
    private BigDecimal receiptAmount;

    @Column(name = "receipt_paid_at")
    private LocalDate receiptPaidAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_source", length = 10)
    private ReceiptSource receiptSource;

    @Column(name = "covered_by_membership_id")
    private UUID coveredByMembershipId;

    @Column(name = "reviewed_by_account_id")
    private UUID reviewedByAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_decision", length = 10)
    private ReviewDecision reviewDecision;

    @Column(name = "review_note", length = 300)
    private String reviewNote;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ElementCollection
    @CollectionTable(name = "contribution_inconsistencies", schema = "contributions",
            joinColumns = @JoinColumn(name = "contribution_id"))
    private List<InconsistencyEmbeddable> inconsistencies = new ArrayList<>();
}
