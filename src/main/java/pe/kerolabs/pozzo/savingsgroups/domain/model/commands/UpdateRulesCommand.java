package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command to change the name and rules of a group that has not started.
 */
public record UpdateRulesCommand(UUID groupId, UUID requesterId, String name, BigDecimal contributionAmount,
                                 Periodicity periodicity, int seats, LocalDate firstContributionDate) {
}
