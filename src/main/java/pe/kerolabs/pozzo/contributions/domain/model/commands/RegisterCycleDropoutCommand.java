package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Records in the cycle of a group that a member dropped out.
 */
public record RegisterCycleDropoutCommand(UUID groupId, UUID membershipId) {
}
