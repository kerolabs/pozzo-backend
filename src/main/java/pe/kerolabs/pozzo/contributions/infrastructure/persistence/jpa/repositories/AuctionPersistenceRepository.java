package pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionStatus;
import pe.kerolabs.pozzo.contributions.infrastructure.persistence.jpa.entities.AuctionPersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for auction persistence entities.
 */
@Repository
public interface AuctionPersistenceRepository extends JpaRepository<AuctionPersistenceEntity, UUID> {

    Optional<AuctionPersistenceEntity> findByPeriodId(UUID periodId);

    List<AuctionPersistenceEntity> findAllByStatus(AuctionStatus status);
}
