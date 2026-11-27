package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Moves a group that has not started to the paid plan, as a demonstration without a real charge.
 */
public record ActivatePaidPlanCommand(UUID groupId, UUID requesterId) {
}
