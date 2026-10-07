package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * Decision of the organizer about a contribution whose receipt did not match.
 *
 * @param reviewerAccountId the organizer who reviewed it
 * @param decision          approve or reject
 * @param note              an optional explanation for the member
 * @param reviewedAt        when it was reviewed
 */
public record Review(UUID reviewerAccountId, ReviewDecision decision, @Nullable String note, Instant reviewedAt) {

    public Review {
        if (note != null && note.length() > 300) {
            throw new IllegalArgumentException("The review note cannot exceed 300 characters");
        }
    }
}
