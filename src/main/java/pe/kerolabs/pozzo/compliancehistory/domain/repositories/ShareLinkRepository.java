package pe.kerolabs.pozzo.compliancehistory.domain.repositories;

import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.ShareLink;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ShareToken;

import java.util.Optional;

/**
 * Share link repository port.
 */
public interface ShareLinkRepository {

    Optional<ShareLink> findByToken(ShareToken token);

    ShareLink save(ShareLink link);
}
