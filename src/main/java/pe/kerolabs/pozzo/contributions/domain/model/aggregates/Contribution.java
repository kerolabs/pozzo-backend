package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionCoveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionRejectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionValidatedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.InconsistencyDetectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Inconsistency;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PaymentReceipt;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Review;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Contribution aggregate root: what a member contributed in a period, with the receipt that proves
 * it, the result of its validation and, when the receipt did not match, the review of the organizer.
 *
 * <p>A receipt is validated at once against the amount, the recipient and the cutoff date. If any of
 * them does not match, the contribution waits for the organizer, who approves or rejects it.</p>
 */
@Getter
public class Contribution extends AbstractDomainAggregateRoot<Contribution> {

    private UUID id;
    private UUID cycleId;
    private UUID periodId;
    private UUID membershipId;
    private @Nullable UUID accountId;
    private Money amount;
    private ContributionMethod method;
    private ContributionStatus status;
    private @Nullable PaymentReceipt receipt;
    private List<Inconsistency> inconsistencies = new ArrayList<>();
    private @Nullable UUID coveredByMembershipId;
    private UUID registeredByAccountId;
    private @Nullable Review review;
    private Instant registeredAt;

    public Contribution() {
    }

    /**
     * Registers a contribution with the data read from its receipt and validates it.
     *
     * @param expectedAmount what the member owes in the period
     * @param cutoffDate     the last day to pay
     * @param payeeName      who must appear as recipient
     */
    public static Contribution fromReceipt(UUID cycleId, UUID periodId, UUID membershipId, UUID accountId,
                                           PaymentReceipt receipt, Money expectedAmount, LocalDate cutoffDate,
                                           String payeeName, Instant now) {
        var contribution = base(cycleId, periodId, membershipId, accountId, receipt.amount(),
                ContributionMethod.TRANSFER, accountId, now);
        contribution.receipt = receipt;
        if (!receipt.amount().isSameAmountAs(expectedAmount)) {
            contribution.inconsistencies.add(new Inconsistency(
                    Inconsistency.AMOUNT, expectedAmount.toString(), receipt.amount().toString()));
        }
        if (!receipt.matchesPayee(payeeName)) {
            contribution.inconsistencies.add(new Inconsistency(Inconsistency.PAYEE, payeeName, receipt.payeeName()));
        }
        if (receipt.paidAt().isAfter(cutoffDate)) {
            contribution.inconsistencies.add(new Inconsistency(
                    Inconsistency.DATE, cutoffDate.toString(), receipt.paidAt().toString()));
        }
        if (contribution.inconsistencies.isEmpty()) {
            contribution.status = ContributionStatus.VALIDATED;
            contribution.registerDomainEvent(new ContributionValidatedEvent(contribution.id, cycleId, periodId,
                    membershipId, accountId, ContributionMethod.TRANSFER, true, now));
        } else {
            contribution.status = ContributionStatus.INCONSISTENT;
            contribution.registerDomainEvent(new InconsistencyDetectedEvent(contribution.id, cycleId, periodId,
                    membershipId, now));
        }
        return contribution;
    }

    /**
     * Records cash the organizer received from a member. It is valid at once, without a receipt.
     */
    public static Contribution inCash(UUID cycleId, UUID periodId, UUID membershipId, @Nullable UUID accountId,
                                      Money amount, LocalDate receivedOn, LocalDate cutoffDate,
                                      UUID organizerAccountId, Instant now) {
        var contribution = base(cycleId, periodId, membershipId, accountId, amount, ContributionMethod.CASH,
                organizerAccountId, now);
        contribution.status = ContributionStatus.VALIDATED;
        contribution.registerDomainEvent(new ContributionValidatedEvent(contribution.id, cycleId, periodId,
                membershipId, accountId, ContributionMethod.CASH, !receivedOn.isAfter(cutoffDate), now));
        return contribution;
    }

    /**
     * Records that a member covered what another member owed; the covered member now owes that amount.
     */
    public static Contribution asCoverage(UUID cycleId, UUID periodId, UUID membershipId, @Nullable UUID accountId,
                                          UUID coveredByMembershipId, Money amount, UUID organizerAccountId,
                                          Instant now) {
        if (membershipId.equals(coveredByMembershipId)) {
            throw new BusinessRuleViolationException("CANNOT_COVER_ONESELF", "A member cannot cover their own contribution");
        }
        var contribution = base(cycleId, periodId, membershipId, accountId, amount, ContributionMethod.COVERAGE,
                organizerAccountId, now);
        contribution.coveredByMembershipId = coveredByMembershipId;
        contribution.status = ContributionStatus.VALIDATED;
        contribution.registerDomainEvent(new ContributionCoveredEvent(contribution.id, cycleId, periodId,
                membershipId, coveredByMembershipId, now));
        return contribution;
    }

    public boolean isUnderReview() {
        return status == ContributionStatus.INCONSISTENT;
    }

    /**
     * Returns true when the contribution settles what the member owed.
     */
    public boolean isValid() {
        return status == ContributionStatus.VALIDATED || status == ContributionStatus.APPROVED;
    }

    /**
     * The organizer accepts a contribution whose receipt did not match.
     */
    public void approve(UUID organizerAccountId, @Nullable String note, LocalDate cutoffDate, Instant now) {
        requireUnderReview();
        this.review = new Review(organizerAccountId, ReviewDecision.APPROVE, note, now);
        this.status = ContributionStatus.APPROVED;
        var onTime = receipt != null && !receipt.paidAt().isAfter(cutoffDate);
        registerDomainEvent(new ContributionValidatedEvent(id, cycleId, periodId, membershipId, accountId,
                method, onTime, now));
    }

    /**
     * The organizer rejects a contribution; the member has to register it again.
     */
    public void reject(UUID organizerAccountId, @Nullable String note, Instant now) {
        requireUnderReview();
        this.review = new Review(organizerAccountId, ReviewDecision.REJECT, note, now);
        this.status = ContributionStatus.REJECTED;
        registerDomainEvent(new ContributionRejectedEvent(id, cycleId, periodId, membershipId, now));
    }

    public List<Inconsistency> getInconsistencies() {
        return Collections.unmodifiableList(inconsistencies);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID cycleId, UUID periodId, UUID membershipId, @Nullable UUID accountId,
                             Money amount, ContributionMethod method, ContributionStatus status,
                             @Nullable PaymentReceipt receipt, List<Inconsistency> inconsistencies,
                             @Nullable UUID coveredByMembershipId, UUID registeredByAccountId,
                             @Nullable Review review, Instant registeredAt) {
        this.id = id;
        this.cycleId = cycleId;
        this.periodId = periodId;
        this.membershipId = membershipId;
        this.accountId = accountId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.receipt = receipt;
        this.inconsistencies = new ArrayList<>(inconsistencies);
        this.coveredByMembershipId = coveredByMembershipId;
        this.registeredByAccountId = registeredByAccountId;
        this.review = review;
        this.registeredAt = registeredAt;
    }

    private void requireUnderReview() {
        if (!isUnderReview()) {
            throw new BusinessRuleViolationException("CONTRIBUTION_NOT_UNDER_REVIEW",
                    "Only a contribution whose receipt did not match can be reviewed");
        }
    }

    private static Contribution base(UUID cycleId, UUID periodId, UUID membershipId, @Nullable UUID accountId,
                                     Money amount, ContributionMethod method, UUID registeredBy, Instant now) {
        var contribution = new Contribution();
        contribution.id = UUID.randomUUID();
        contribution.cycleId = cycleId;
        contribution.periodId = periodId;
        contribution.membershipId = membershipId;
        contribution.accountId = accountId;
        contribution.amount = amount;
        contribution.method = method;
        contribution.registeredByAccountId = registeredBy;
        contribution.registeredAt = now;
        return contribution;
    }
}
