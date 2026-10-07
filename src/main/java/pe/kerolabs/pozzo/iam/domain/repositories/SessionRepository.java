package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;

import java.util.Optional;
import java.util.UUID;

/**
 * Session repository port.
 */
public interface SessionRepository {

    Optional<Session> findById(UUID id);

    Optional<Session> findByTokenHash(String tokenHash);

    Session save(Session session);
}
