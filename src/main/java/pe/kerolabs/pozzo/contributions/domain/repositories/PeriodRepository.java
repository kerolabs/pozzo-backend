package pe.kerolabs.pozzo.contributions.domain.repositories;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Period repository port.
 */
public interface PeriodRepository {

    Optional<Period> findById(UUID id);

    Optional<Period> findByCycleIdAndTurnNumber(UUID cycleId, int turnNumber);

    /**
     * The periods of a cycle in turn order.
     */
    List<Period> findAllByCycleId(UUID cycleId);

    Period save(Period period);
}
