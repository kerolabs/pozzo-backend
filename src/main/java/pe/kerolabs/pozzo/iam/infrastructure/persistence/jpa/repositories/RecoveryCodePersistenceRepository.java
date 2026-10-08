package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.RecoveryCodePersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for recovery code persistence entities.
 */
@Repository
public interface RecoveryCodePersistenceRepository extends JpaRepository<RecoveryCodePersistenceEntity, UUID> {

    Optional<RecoveryCodePersistenceEntity> findFirstByEmailOrderByIssuedAtDesc(String email);

    Optional<RecoveryCodePersistenceEntity> findFirstByEmailAndStatusOrderByIssuedAtDesc(String email,
                                                                                        VerificationStatus status);

    List<RecoveryCodePersistenceEntity> findAllByEmailAndStatus(String email, VerificationStatus status);
}
