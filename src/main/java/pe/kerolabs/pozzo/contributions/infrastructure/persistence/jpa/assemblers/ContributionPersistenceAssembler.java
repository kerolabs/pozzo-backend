package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Inconsistency;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PaymentReceipt;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Review;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.embeddables.InconsistencyEmbeddable;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.ContributionPersistenceEntity;

/**
 * Static assembler between the contribution aggregate and its persistence entity.
 */
public final class ContributionPersistenceAssembler {

    private ContributionPersistenceAssembler() {
    }

    public static Contribution toDomainFromPersistence(ContributionPersistenceEntity entity) {
        var currency = entity.getCurrency();
        var receipt = entity.getReceiptOperationNumber() == null
                ? null
                : new PaymentReceipt(entity.getReceiptOperationNumber(), entity.getReceiptPayer(),
                        entity.getReceiptPayee(), Money.of(entity.getReceiptAmount(), currency),
                        entity.getReceiptPaidAt(), entity.getReceiptSource());
        var review = entity.getReviewDecision() == null
                ? null
                : new Review(entity.getReviewedByAccountId(), entity.getReviewDecision(), entity.getReviewNote(),
                        entity.getReviewedAt());
        var inconsistencies = entity.getInconsistencies().stream()
                .map(item -> new Inconsistency(item.getField(), item.getExpectedValue(), item.getFoundValue()))
                .toList();
        var contribution = new Contribution();
        contribution.restoreState(entity.getId(), entity.getCycleId(), entity.getPeriodId(), entity.getMembershipId(),
                entity.getAccountId(), Money.of(entity.getAmount(), currency), entity.getMethod(), entity.getStatus(),
                receipt, inconsistencies, entity.getCoveredByMembershipId(), entity.getRegisteredByAccountId(),
                review, entity.getRegisteredAt());
        return contribution;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static ContributionPersistenceEntity toPersistenceFromDomain(Contribution contribution,
                                                                        ContributionPersistenceEntity entity) {
        entity.setId(contribution.getId());
        entity.setCycleId(contribution.getCycleId());
        entity.setPeriodId(contribution.getPeriodId());
        entity.setMembershipId(contribution.getMembershipId());
        entity.setAccountId(contribution.getAccountId());
        entity.setAmount(contribution.getAmount().amount());
        entity.setCurrency(contribution.getAmount().currency());
        entity.setMethod(contribution.getMethod());
        entity.setStatus(contribution.getStatus());
        entity.setRegisteredAt(contribution.getRegisteredAt());
        entity.setRegisteredByAccountId(contribution.getRegisteredByAccountId());
        var receipt = contribution.getReceipt();
        entity.setReceiptOperationNumber(receipt == null ? null : receipt.operationNumber());
        entity.setReceiptPayer(receipt == null ? null : receipt.payerName());
        entity.setReceiptPayee(receipt == null ? null : receipt.payeeName());
        entity.setReceiptAmount(receipt == null ? null : receipt.amount().amount());
        entity.setReceiptPaidAt(receipt == null ? null : receipt.paidAt());
        entity.setReceiptSource(receipt == null ? null : receipt.source());
        entity.setCoveredByMembershipId(contribution.getCoveredByMembershipId());
        var review = contribution.getReview();
        entity.setReviewedByAccountId(review == null ? null : review.reviewerAccountId());
        entity.setReviewDecision(review == null ? null : review.decision());
        entity.setReviewNote(review == null ? null : review.note());
        entity.setReviewedAt(review == null ? null : review.reviewedAt());
        entity.getInconsistencies().clear();
        contribution.getInconsistencies().forEach(item -> entity.getInconsistencies().add(
                new InconsistencyEmbeddable(item.field(), item.expected(), item.found())));
        return entity;
    }
}
