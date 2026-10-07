package pe.kerolabs.pozzo.contributions.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;

import java.util.UUID;

/**
 * Command for the organizer to approve or reject a contribution whose receipt did not match.
 */
public record ReviewContributionCommand(UUID contributionId, UUID requesterAccountId, ReviewDecision decision,
                                        @Nullable String note) {
}
