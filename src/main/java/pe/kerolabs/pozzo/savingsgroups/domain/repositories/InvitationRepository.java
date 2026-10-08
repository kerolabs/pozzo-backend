package pe.kerolabs.pozzo.savingsgroups.domain.repositories;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Invitation repository port.
 */
public interface InvitationRepository {

    Optional<Invitation> findByCode(InvitationCode code);

    boolean existsByCode(InvitationCode code);

    /**
     * The active invitations of a group; there should be at most one.
     */
    List<Invitation> findAllActiveByGroupId(UUID groupId);

    Invitation save(Invitation invitation);

    void deleteAllByGroupId(UUID groupId);
}
