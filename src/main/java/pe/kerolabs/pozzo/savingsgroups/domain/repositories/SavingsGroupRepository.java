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

    /**
     * The groups with a member the organizer registered without the application with that number, who has not
     * linked an account yet.
     *
     * @param phone the number in E.164 format
     */
    List<SavingsGroup> findAllWithUnlinkedManualMember(String phone);

    SavingsGroup save(SavingsGroup group);

    /**
     * Deletes the group with its memberships and turns, and publishes its pending domain events.
     */
    void delete(SavingsGroup group);
}
