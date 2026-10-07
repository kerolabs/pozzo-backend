package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.RecoveryCode;

import java.util.List;
import java.util.Optional;

/**
 * Recovery code repository port. Codes are found by the backup email they were sent to.
 */
public interface RecoveryCodeRepository {

    /**
     * The most recent code sent to the email, whatever its status.
     */
    Optional<RecoveryCode> findLatestByEmail(String email);

    /**
     * The most recent pending code sent to the email.
     */
    Optional<RecoveryCode> findPendingByEmail(String email);

    /**
     * Every pending code sent to the email.
     */
    List<RecoveryCode> findAllPendingByEmail(String email);

    RecoveryCode save(RecoveryCode recoveryCode);
}
