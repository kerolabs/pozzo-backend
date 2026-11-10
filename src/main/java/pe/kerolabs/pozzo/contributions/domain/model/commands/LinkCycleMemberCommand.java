package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Gives the turn of a member who took part without the application to the account they signed up with.
 */
public record LinkCycleMemberCommand(UUID groupId, UUID membershipId, UUID accountId) {
}
