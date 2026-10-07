package pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.compliancehistory.infrastructure.persistence.jpa.entities.MemberRecordPersistenceEntity;

import java.util.UUID;

/**
 * Spring Data repository for member record persistence entities.
 */
@Repository
public interface MemberRecordPersistenceRepository extends JpaRepository<MemberRecordPersistenceEntity, UUID> {
}
