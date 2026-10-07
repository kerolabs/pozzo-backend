package pe.kerolabs.pozzo.savingsgroups.domain.repositories;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Savings group repository port.
 */
public interface SavingsGroupRepository {

    Optional<SavingsGroup> findById(UUID id);

    /**
     * The groups where the account has an active membership.
     */
    List<SavingsGroup> findAllByMemberId(UUID memberId);

    SavingsGroup save(SavingsGroup group);
}
