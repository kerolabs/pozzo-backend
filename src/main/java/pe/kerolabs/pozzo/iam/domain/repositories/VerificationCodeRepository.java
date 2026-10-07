package pe.kerolabs.pozzo.iam.domain.repositories;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.List;
import java.util.Optional;

/**
 * Verification code repository port.
 */
public interface VerificationCodeRepository {

    /**
     * The most recent code issued for the number, whatever its status.
     */
    Optional<VerificationCode> findLatestByPhoneNumber(PhoneNumber phoneNumber);

    /**
     * The most recent pending code issued for the number.
     */
    Optional<VerificationCode> findPendingByPhoneNumber(PhoneNumber phoneNumber);

    /**
     * Every pending code issued for the number.
     */
    List<VerificationCode> findAllPendingByPhoneNumber(PhoneNumber phoneNumber);

    VerificationCode save(VerificationCode verificationCode);
}
