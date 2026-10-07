package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.PeriodPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for period persistence entities.
 */
@Repository
public interface PeriodPersistenceRepository extends JpaRepository<PeriodPersistenceEntity, UUID> {

    Optional<PeriodPersistenceEntity> findByCycleIdAndTurnNumber(UUID cycleId, int turnNumber);

    List<PeriodPersistenceEntity> findAllByCycleIdOrderByTurnNumberAsc(UUID cycleId);
}
