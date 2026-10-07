package pe.kerolabs.pozzo.contributions.domain.repositories;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;

import java.util.Optional;
import java.util.UUID;

/**
 * Cycle repository port.
 */
public interface CycleRepository {

    Optional<Cycle> findById(UUID id);

    Optional<Cycle> findByGroupId(UUID groupId);

    boolean existsByGroupId(UUID groupId);

    Cycle save(Cycle cycle);
}
