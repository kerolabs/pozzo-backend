package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.CyclePersistenceEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for cycle persistence entities.
 */
@Repository
public interface CyclePersistenceRepository extends JpaRepository<CyclePersistenceEntity, UUID> {

    Optional<CyclePersistenceEntity> findByGroupId(UUID groupId);

    boolean existsByGroupId(UUID groupId);
}
