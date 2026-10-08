package pe.kerolabs.pozzo.compliancehistory.domain.model.queries;

/**
 * Query for the summary behind a share link; public, without a session.
 */
public record GetSharedHistoryQuery(String token) {
}
