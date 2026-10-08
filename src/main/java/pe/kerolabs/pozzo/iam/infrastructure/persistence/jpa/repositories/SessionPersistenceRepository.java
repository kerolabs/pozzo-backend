package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.SessionPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for session persistence entities.
 */
@Repository
public interface SessionPersistenceRepository extends JpaRepository<SessionPersistenceEntity, UUID> {

    Optional<SessionPersistenceEntity> findByTokenHash(String tokenHash);

    List<SessionPersistenceEntity> findAllByAccountIdAndRevokedAtIsNull(UUID accountId);
}
