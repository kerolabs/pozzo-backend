package pe.kerolabs.pozzo.contributions.domain.repositories;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contribution repository port.
 */
public interface ContributionRepository {

    Optional<Contribution> findById(UUID id);

    /**
     * The contributions of a period, the most recent first.
     */
    List<Contribution> findAllByPeriodId(UUID periodId);

    /**
     * The contributions of a member in a cycle, the most recent first.
     */
    List<Contribution> findAllByCycleIdAndMembershipId(UUID cycleId, UUID membershipId);

    /**
     * Whether a receipt with this operation number was already used in the cycle.
     */
    boolean existsByCycleIdAndOperationNumber(UUID cycleId, String operationNumber);

    Contribution save(Contribution contribution);
}
