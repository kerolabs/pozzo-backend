package pe.kerolabs.pozzo.iam.domain.model.queries;

import java.util.Collection;
import java.util.UUID;

/**
 * Query for several accounts at once, e.g. the members of a group.
 *
 * @param accountIds the account identifiers; the unknown ones are left out
 */
public record GetAccountsQuery(Collection<UUID> accountIds) {
}
