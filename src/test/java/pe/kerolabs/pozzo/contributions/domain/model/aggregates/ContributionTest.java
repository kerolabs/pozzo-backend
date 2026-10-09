package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionCoveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionRejectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionValidatedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.InconsistencyDetectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Inconsistency;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PaymentReceipt;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReceiptSource;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Contribution: validation of a receipt and review by the organizer")
class ContributionTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");
    private static final LocalDate CUTOFF = LocalDate.parse("2026-10-20");
    private static final Money EXPECTED = soles("200");

    private final UUID cycleId = UUID.randomUUID();
    private final UUID periodId = UUID.randomUUID();
    private final UUID membershipId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final UUID organizerId = UUID.randomUUID();

    private static Money soles(String amount) {
        return Money.of(new BigDecimal(amount), Money.PEN);
    }

    private static PaymentReceipt receipt(String amount, String payee, String paidAt) {
        return new PaymentReceipt("04581273", "Sofia Gonzales", payee, soles(amount), LocalDate.parse(paidAt),
                ReceiptSource.YAPE);
    }

    private Contribution register(PaymentReceipt receipt) {
        return Contribution.fromReceipt(cycleId, periodId, membershipId, accountId, receipt, EXPECTED, CUTOFF,
                "Anna Weber", NOW);
    }

    @Test
    void validatesAtOnceAReceiptThatMatchesAmountPayeeAndDate() {
        var contribution = register(receipt("200.00", "Anna Weber", "2026-10-08"));

        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.VALIDATED);
        assertThat(contribution.getInconsistencies()).isEmpty();
        assertThat(contribution.isValid()).isTrue();
        assertThat(contribution.domainEvents()).singleElement().isInstanceOf(ContributionValidatedEvent.class);
    }

    @Test
    void marksAsInconsistentAReceiptWithAnotherAmount() {
        var contribution = register(receipt("150", "Anna Weber", "2026-10-08"));

        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.INCONSISTENT);
        assertThat(contribution.getInconsistencies()).containsExactly(
                new Inconsistency(Inconsistency.AMOUNT, "S/ 200.00", "S/ 150.00"));
        assertThat(contribution.isValid()).isFalse();
        assertThat(contribution.domainEvents()).singleElement().isInstanceOf(InconsistencyDetectedEvent.class);
    }

    @Test
    void reportsEveryFieldThatDoesNotMatch() {
        var contribution = register(receipt("150", "Carla Vega", "2026-10-21"));

        assertThat(contribution.getInconsistencies()).extracting(Inconsistency::field)
                .containsExactly(Inconsistency.AMOUNT, Inconsistency.PAYEE, Inconsistency.DATE);
    }

    @Test
    void acceptsAPaymentMadeOnTheCutoffDate() {
        var contribution = register(receipt("200", "Anna Weber", "2026-10-20"));

        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.VALIDATED);
    }

    @Test
    void approvalByTheOrganizerSettlesAnInconsistentContribution() {
        var contribution = register(receipt("150", "Anna Weber", "2026-10-08"));
        contribution.clearDomainEvents();

        contribution.approve(organizerId, "Paid the rest in cash", CUTOFF, NOW);

        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.APPROVED);
        assertThat(contribution.isValid()).isTrue();
        assertThat(contribution.getReview()).isNotNull();
        assertThat(contribution.getReview().decision()).isEqualTo(ReviewDecision.APPROVE);
        assertThat(contribution.domainEvents()).singleElement().isInstanceOf(ContributionValidatedEvent.class);
    }

    @Test
    void rejectionLetsTheMemberRegisterAgain() {
        var contribution = register(receipt("150", "Anna Weber", "2026-10-08"));
        contribution.clearDomainEvents();

        contribution.reject(organizerId, "The receipt is from another group", NOW);

        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.REJECTED);
        assertThat(contribution.isValid()).isFalse();
        assertThat(contribution.domainEvents()).singleElement().isInstanceOf(ContributionRejectedEvent.class);
    }

    @Test
    void onlyAContributionUnderReviewCanBeReviewed() {
        var contribution = register(receipt("200", "Anna Weber", "2026-10-08"));

        assertThatThrownBy(() -> contribution.approve(organizerId, null, CUTOFF, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("CONTRIBUTION_NOT_UNDER_REVIEW");
    }

    @Test
    void cashIsValidWithoutAReceipt() {
        var contribution = Contribution.inCash(cycleId, periodId, membershipId, accountId, EXPECTED,
                LocalDate.parse("2026-10-10"), CUTOFF, organizerId, NOW);

        assertThat(contribution.getMethod()).isEqualTo(ContributionMethod.CASH);
        assertThat(contribution.getStatus()).isEqualTo(ContributionStatus.VALIDATED);
        assertThat(contribution.getReceipt()).isNull();
    }

    @Test
    void aCoverageRecordsWhoPutTheMoney() {
        var coveredBy = UUID.randomUUID();

        var contribution = Contribution.asCoverage(cycleId, periodId, membershipId, accountId, coveredBy, EXPECTED,
                organizerId, NOW);

        assertThat(contribution.getMethod()).isEqualTo(ContributionMethod.COVERAGE);
        assertThat(contribution.getCoveredByMembershipId()).isEqualTo(coveredBy);
        assertThat(contribution.domainEvents()).singleElement().isInstanceOf(ContributionCoveredEvent.class);
    }

    @Test
    void aMemberCannotCoverTheirOwnContribution() {
        assertThatThrownBy(() -> Contribution.asCoverage(cycleId, periodId, membershipId, accountId, membershipId,
                EXPECTED, organizerId, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("CANNOT_COVER_ONESELF");
    }

    @Test
    void onlyATransferKeepsTheImageOfItsReceipt() {
        var transfer = register(receipt("200", "Anna Weber", "2026-10-08"));
        var cash = Contribution.inCash(cycleId, periodId, membershipId, accountId, EXPECTED, CUTOFF, CUTOFF,
                organizerId, NOW);

        transfer.attachReceiptImage("receipts/one.jpg");

        assertThat(transfer.hasReceiptImage()).isTrue();
        assertThatThrownBy(() -> cash.attachReceiptImage("receipts/two.jpg"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("RECEIPT_IMAGE_NOT_ALLOWED");
    }
}
