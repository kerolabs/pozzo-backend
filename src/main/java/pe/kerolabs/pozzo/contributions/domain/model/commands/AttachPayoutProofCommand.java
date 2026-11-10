package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Keeps the image of the transfer with which the organizer delivered the pot of a period.
 */
public record AttachPayoutProofCommand(UUID periodId, UUID requesterAccountId, byte[] content, String contentType) {
}
