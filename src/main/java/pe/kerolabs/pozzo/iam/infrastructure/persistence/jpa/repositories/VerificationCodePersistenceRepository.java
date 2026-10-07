package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.VerificationCodePersistenceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for verification code persistence entities.
 */
@Repository
public interface VerificationCodePersistenceRepository extends JpaRepository<VerificationCodePersistenceEntity, UUID> {

    Optional<VerificationCodePersistenceEntity> findFirstByPhoneNumberOrderByIssuedAtDesc(String phoneNumber);

    Optional<VerificationCodePersistenceEntity> findFirstByPhoneNumberAndStatusOrderByIssuedAtDesc(
            String phoneNumber, VerificationStatus status);

    List<VerificationCodePersistenceEntity> findAllByPhoneNumberAndStatus(String phoneNumber, VerificationStatus status);
}
