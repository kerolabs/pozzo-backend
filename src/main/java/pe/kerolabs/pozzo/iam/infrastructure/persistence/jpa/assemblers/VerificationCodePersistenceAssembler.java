package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.VerificationCodePersistenceEntity;

/**
 * Static assembler between the verification code aggregate and its persistence entity.
 */
public final class VerificationCodePersistenceAssembler {

    private VerificationCodePersistenceAssembler() {
    }

    public static VerificationCode toDomainFromPersistence(VerificationCodePersistenceEntity entity) {
        var code = new VerificationCode();
        code.restoreState(
                entity.getId(),
                PhoneNumber.fromE164(entity.getPhoneNumber()),
                entity.getCodeHash(),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                entity.getAttempts(),
                entity.getStatus());
        return code;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static VerificationCodePersistenceEntity toPersistenceFromDomain(
            VerificationCode code, VerificationCodePersistenceEntity entity) {
        entity.setId(code.getId());
        entity.setPhoneNumber(code.getPhoneNumber().e164());
        entity.setCodeHash(code.getCodeHash());
        entity.setIssuedAt(code.getIssuedAt());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setAttempts(code.getAttempts());
        entity.setStatus(code.getStatus());
        return entity;
    }
}
