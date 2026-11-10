package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Opens the next cycle of a group whose cycle was over, with the same rules and members.
 */
public record OpenNewCycleCommand(UUID groupId, UUID requesterId, LocalDate firstContributionDate) {
}
