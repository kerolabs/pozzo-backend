package pe.kerolabs.pozzo.compliancehistory.domain.repositories;

import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.ShareLink;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;

import java.util.Optional;

/**
 * Share link repository port.
 */
public interface ShareLinkRepository {

    /**
     * Finds the share link that owns the given token, revoked or not.
     */
    Optional<ShareLink> findByToken(ShareToken token);

    /**
     * Creates or updates the share link and returns the stored state.
     */
    ShareLink save(ShareLink link);
}
