package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.RecoveryCode;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.RecoveryCodePersistenceEntity;

/**
 * Static assembler between the recovery code aggregate and its persistence entity.
 */
public final class RecoveryCodePersistenceAssembler {

    private RecoveryCodePersistenceAssembler() {
    }

    public static RecoveryCode toDomainFromPersistence(RecoveryCodePersistenceEntity entity) {
        var code = new RecoveryCode();
        code.restoreState(
                entity.getId(),
                entity.getAccountId(),
                entity.getEmail(),
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
    public static RecoveryCodePersistenceEntity toPersistenceFromDomain(RecoveryCode code,
                                                                        RecoveryCodePersistenceEntity entity) {
        entity.setId(code.getId());
        entity.setAccountId(code.getAccountId());
        entity.setEmail(code.getEmail());
        entity.setCodeHash(code.getCodeHash());
        entity.setIssuedAt(code.getIssuedAt());
        entity.setExpiresAt(code.getExpiresAt());
        entity.setAttempts(code.getAttempts());
        entity.setStatus(code.getStatus());
        return entity;
    }
}
