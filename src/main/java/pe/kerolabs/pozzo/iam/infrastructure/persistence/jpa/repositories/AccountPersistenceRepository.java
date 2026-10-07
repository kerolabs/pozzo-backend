package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.AccountStatus;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for account persistence entities.
 */
@Repository
public interface AccountPersistenceRepository extends JpaRepository<AccountPersistenceEntity, UUID> {

    Optional<AccountPersistenceEntity> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<AccountPersistenceEntity> findFirstByBackupEmailAndStatus(String backupEmail, AccountStatus status);

    boolean existsByBackupEmailAndIdNot(String backupEmail, UUID id);
}
