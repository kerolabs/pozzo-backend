package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.ComplianceEntryPersistenceEntity;

import java.util.UUID;

/**
 * Spring Data repository for compliance entry persistence entities.
 */
@Repository
public interface ComplianceEntryPersistenceRepository extends JpaRepository<ComplianceEntryPersistenceEntity, UUID> {

    boolean existsBySourceEventId(String sourceEventId);
}
