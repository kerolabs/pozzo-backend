package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Session repository port.
 */
public interface SessionRepository {

    Optional<Session> findById(UUID id);

    Optional<Session> findByTokenHash(String tokenHash);

    /**
     * The sessions of an account that have not been revoked, expired or not.
     */
    List<Session> findAllNotRevokedByAccountId(UUID accountId);

    Session save(Session session);
}
