package pe.kerolabs.pozzo.iam.domain.model.queries;

import java.util.UUID;

/**
 * Query for the account, and with it the profile, of a member.
 *
 * @param accountId the account of the member
 */
public record GetProfileQuery(UUID accountId) {
}
