package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import java.util.UUID;

/**
 * Gives two members of a started group the turns Contributions swapped.
 */
public record ApplySwappedTurnsCommand(UUID groupId, UUID firstMembershipId, int firstTurn, UUID secondMembershipId,
                                       int secondTurn) {
}
