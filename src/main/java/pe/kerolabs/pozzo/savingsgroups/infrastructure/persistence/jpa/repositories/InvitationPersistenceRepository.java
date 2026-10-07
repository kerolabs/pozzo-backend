package pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.InvitationStatus;
import pe.kerolabs.pozzo.savingsgroups.infrastructure.persistence.jpa.entities.InvitationPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for invitation persistence entities.
 */
@Repository
public interface InvitationPersistenceRepository extends JpaRepository<InvitationPersistenceEntity, UUID> {

    Optional<InvitationPersistenceEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<InvitationPersistenceEntity> findAllByGroupIdAndStatus(UUID groupId, InvitationStatus status);
}
