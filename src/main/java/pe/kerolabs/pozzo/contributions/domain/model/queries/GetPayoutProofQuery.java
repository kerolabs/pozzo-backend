package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * A temporary link to the image of the transfer with which the pot of a period was delivered.
 */
public record GetPayoutProofQuery(UUID periodId, UUID requesterAccountId) {
}
