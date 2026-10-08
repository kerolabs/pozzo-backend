package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Command to keep the image of the receipt of a contribution, so the member and the organizer can see it later.
 *
 * @param contributionId     the contribution the receipt proves
 * @param requesterAccountId the member who registered it
 * @param content            the image bytes
 * @param contentType        the media type of the image
 */
public record AttachReceiptImageCommand(UUID contributionId, UUID requesterAccountId, byte[] content,
                                        String contentType) {
}
