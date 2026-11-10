package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionFallback;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionResolution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionSettings;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Auction: the members who have not collected bid a discount for the turn")
class AuctionTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");
    private static final LocalDate TODAY = LocalDate.parse("2026-10-08");

    private final UUID organizer = UUID.randomUUID();
    private final UUID anna = UUID.randomUUID();
    private final UUID sofia = UUID.randomUUID();
    private final UUID jorge = UUID.randomUUID();
    private Auction auction;

    private static Money soles(String amount) {
        return Money.of(new BigDecimal(amount), Money.PEN);
    }

    @BeforeEach
    void openTheAuctionOfTheFirstTurn() {
        var cycle = Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizer, PeriodTest.monthlyRules(),
                List.of(new CycleTurn(1, anna, organizer, "Anna Weber"),
                        new CycleTurn(2, sofia, UUID.randomUUID(), "Sofia Gonzales"),
                        new CycleTurn(3, jorge, UUID.randomUUID(), "Jorge Ramos")),
                new AuctionSettings(2, soles("20"), AuctionFallback.DRAW), NOW);
        auction = Auction.open(cycle, Period.open(cycle, 1, NOW), NOW);
    }

    @Test
    void closesTwoDaysBeforeTheCutoffOverThePotOfTheThreeMembers() {
        assertThat(auction.getClosesOn()).isEqualTo(LocalDate.parse("2026-10-18"));
        assertThat(auction.getPotAmount().amount()).isEqualByComparingTo("600");
    }

    @Test
    void theHighestBidWinsAndTheFirstOnePlacedBreaksATie() {
        auction.placeBid(sofia, soles("50"), List.of(anna, sofia, jorge), TODAY, NOW);
        auction.placeBid(jorge, soles("50"), List.of(anna, sofia, jorge), TODAY, NOW.plusSeconds(60));

        assertThat(auction.highestBid()).hasValueSatisfying(bid -> assertThat(bid.membershipId()).isEqualTo(sofia));
        assertThat(auction.rankedBids()).extracting(bid -> bid.membershipId()).containsExactly(sofia, jorge);
    }

    @Test
    void aNewBidReplacesThePreviousOneOfTheSameMember() {
        auction.placeBid(sofia, soles("30"), List.of(anna, sofia, jorge), TODAY, NOW);
        auction.placeBid(sofia, soles("45"), List.of(anna, sofia, jorge), TODAY, NOW.plusSeconds(60));

        assertThat(auction.getBids()).singleElement()
                .satisfies(bid -> assertThat(bid.amount().amount()).isEqualByComparingTo("45"));
    }

    @Test
    void rejectsBidsBelowTheMinimumNotBelowThePotFromWhoCollectedAndAfterTheClosingDay() {
        assertThatThrownBy(() -> auction.placeBid(sofia, soles("10"), List.of(sofia), TODAY, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("BID_BELOW_MINIMUM");
        assertThatThrownBy(() -> auction.placeBid(sofia, soles("600"), List.of(sofia), TODAY, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("BID_TOO_HIGH");
        assertThatThrownBy(() -> auction.placeBid(sofia, soles("50"), List.of(jorge), TODAY, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("ALREADY_COLLECTED");
        assertThatThrownBy(() -> auction.placeBid(sofia, soles("50"), List.of(sofia),
                LocalDate.parse("2026-10-19"), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("AUCTION_CLOSED");
    }

    @Test
    void theDiscountIsSharedByTheMembersWhoStillHaveNotCollected() {
        auction.placeBid(sofia, soles("50"), List.of(anna, sofia, jorge), TODAY, NOW);

        auction.close(sofia, AuctionResolution.BID, 2, NOW);

        assertThat(auction.isOpen()).isFalse();
        assertThat(auction.getDiscount().amount()).isEqualByComparingTo("50");
        assertThat(auction.getCreditPerMember().amount()).isEqualByComparingTo("25");
    }

    @Test
    void withoutBidsTheTurnGoesWithoutDiscount() {
        auction.close(jorge, AuctionResolution.DRAW, 2, NOW);

        assertThat(auction.getDiscount().amount()).isEqualByComparingTo("0");
        assertThat(auction.getCreditPerMember().amount()).isEqualByComparingTo("0");
    }
}
