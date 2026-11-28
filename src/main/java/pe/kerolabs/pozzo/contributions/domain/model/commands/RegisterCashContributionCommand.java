package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for the organizer to record cash received from a member.
 *
 * @param differenceConfirmed whether the organizer confirmed an amount other than what the member owes
 */
public record RegisterCashContributionCommand(UUID periodId, UUID requesterAccountId, UUID membershipId,
                                              BigDecimal amount, LocalDate receivedOn, boolean differenceConfirmed) {
}
