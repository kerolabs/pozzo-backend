package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * The organizer takes back the approval or the rejection of a contribution.
 */
public record ReopenReviewCommand(UUID contributionId, UUID requesterAccountId) {
}
