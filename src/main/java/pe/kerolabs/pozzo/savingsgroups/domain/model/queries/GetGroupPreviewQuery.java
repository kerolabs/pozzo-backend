package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

/**
 * Query for the summary of a group shown before joining it with an invitation code.
 */
public record GetGroupPreviewQuery(String invitationCode) {
}
