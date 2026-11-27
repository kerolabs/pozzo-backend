package pe.kerolabs.pozzo.contributions.domain.model.entities;

import lombok.Getter;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;

import java.time.LocalDate;
import java.util.UUID;

/**
 * What a member owes in a period and whether it is already settled. It lives inside its Period.
 */
@Getter
public class ExpectedContribution {

    private UUID id;
    private UUID membershipId;
    private Money amount;
    private ExpectedStatus status;
    private @Nullable UUID settledByContributionId;

    public ExpectedContribution() {
    }

    public static ExpectedContribution pendingFor(UUID membershipId, Money amount) {
        var expected = new ExpectedContribution();
        expected.id = UUID.randomUUID();
        expected.membershipId = membershipId;
        expected.amount = amount;
        expected.status = ExpectedStatus.PENDING;
        return expected;
    }

    /**
     * Moves what is still owed to the member who replaced the one who dropped out.
     */
    public void transferTo(UUID membershipId) {
        if (!isPending()) {
            throw new IllegalStateException("Only a pending contribution moves to a replacement");
        }
        this.membershipId = membershipId;
    }

    public boolean isPending() {
        return status == ExpectedStatus.PENDING;
    }

    /**
     * A pending contribution is late once its cutoff date has passed.
     */
    public boolean isLate(LocalDate today, LocalDate cutoffDate) {
        return isPending() && today.isAfter(cutoffDate);
    }

    /**
     * Marks what was owed as paid or covered by a contribution.
     */
    public void settle(ExpectedStatus how, UUID contributionId) {
        if (how == ExpectedStatus.PENDING) {
            throw new IllegalArgumentException("A contribution is settled as paid or covered");
        }
        this.status = how;
        this.settledByContributionId = contributionId;
    }

    /**
     * Takes back the contribution that settled what was owed: the member owes it again.
     */
    public void unsettle(UUID contributionId) {
        if (!contributionId.equals(settledByContributionId)) {
            throw new BusinessRuleViolationException("CONTRIBUTION_NOT_SETTLING",
                    "The contribution did not settle what the member owed");
        }
        this.status = ExpectedStatus.PENDING;
        this.settledByContributionId = null;
    }

    /**
     * Restores the entity from persistence.
     */
    public void restoreState(UUID id, UUID membershipId, Money amount, ExpectedStatus status,
                             @Nullable UUID settledByContributionId) {
        this.id = id;
        this.membershipId = membershipId;
        this.amount = amount;
        this.status = status;
        this.settledByContributionId = settledByContributionId;
    }
}
