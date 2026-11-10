package pe.kerolabs.pozzo.contributions.domain.model.queries;

/**
 * The contribution a public verification link points to. Anyone with the link can ask.
 */
public record GetVerifiedContributionQuery(String token) {
}
