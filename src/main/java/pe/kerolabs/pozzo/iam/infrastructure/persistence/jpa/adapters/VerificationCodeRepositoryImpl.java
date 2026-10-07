package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.adapters;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.domain.repositories.VerificationCodeRepository;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers.VerificationCodePersistenceAssembler;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.VerificationCodePersistenceEntity;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.repositories.VerificationCodePersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements the verification code repository port with Spring Data JPA
 * and publishes the domain events of each saved aggregate.
 */
@Repository
public class VerificationCodeRepositoryImpl implements VerificationCodeRepository {

    private final VerificationCodePersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VerificationCodeRepositoryImpl(VerificationCodePersistenceRepository persistenceRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<VerificationCode> findLatestByPhoneNumber(PhoneNumber phoneNumber) {
        return persistenceRepository.findFirstByPhoneNumberOrderByIssuedAtDesc(phoneNumber.e164())
                .map(VerificationCodePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<VerificationCode> findPendingByPhoneNumber(PhoneNumber phoneNumber) {
        return persistenceRepository
                .findFirstByPhoneNumberAndStatusOrderByIssuedAtDesc(phoneNumber.e164(), VerificationStatus.PENDING)
                .map(VerificationCodePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<VerificationCode> findAllPendingByPhoneNumber(PhoneNumber phoneNumber) {
        return persistenceRepository.findAllByPhoneNumberAndStatus(phoneNumber.e164(), VerificationStatus.PENDING)
                .stream()
                .map(VerificationCodePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public VerificationCode save(VerificationCode verificationCode) {
        var entity = persistenceRepository.findById(verificationCode.getId())
                .orElseGet(VerificationCodePersistenceEntity::new);
        var saved = persistenceRepository.save(
                VerificationCodePersistenceAssembler.toPersistenceFromDomain(verificationCode, entity));
        verificationCode.domainEvents().forEach(eventPublisher::publishEvent);
        verificationCode.clearDomainEvents();
        return VerificationCodePersistenceAssembler.toDomainFromPersistence(saved);
    }
}
