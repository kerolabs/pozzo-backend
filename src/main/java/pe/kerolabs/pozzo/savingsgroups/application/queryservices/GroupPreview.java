package pe.kerolabs.pozzo.savingsgroups.application.queryservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;

/**
 * What a member sees before joining with an invitation code. The REST layer exposes only the name,
 * the organizer, the rules and the free seats, never the list of members.
 *
 * @param group      the group of the invitation
 * @param invitation the invitation that was typed
 */
public record GroupPreview(SavingsGroup group, Invitation invitation) {
}
