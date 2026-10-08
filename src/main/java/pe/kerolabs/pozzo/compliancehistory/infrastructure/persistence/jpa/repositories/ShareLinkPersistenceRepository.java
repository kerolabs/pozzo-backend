package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.ShareLinkPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for share link persistence entities.
 */
@Repository
public interface ShareLinkPersistenceRepository extends JpaRepository<ShareLinkPersistenceEntity, UUID> {

    Optional<ShareLinkPersistenceEntity> findByToken(String token);
}
