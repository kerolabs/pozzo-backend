package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.ContributionPersistenceEntity;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for contribution persistence entities.
 */
@Repository
public interface ContributionPersistenceRepository extends JpaRepository<ContributionPersistenceEntity, UUID> {

    List<ContributionPersistenceEntity> findAllByPeriodIdOrderByRegisteredAtDesc(UUID periodId);

    List<ContributionPersistenceEntity> findAllByCycleIdAndMembershipIdOrderByRegisteredAtDesc(UUID cycleId,
                                                                                               UUID membershipId);

    boolean existsByCycleIdAndReceiptOperationNumber(UUID cycleId, String receiptOperationNumber);
}
