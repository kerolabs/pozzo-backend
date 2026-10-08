package pe.kerolabs.pozzo.iam.application.internal.commandservices;

import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.TestPhoneNumbers;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.verification.VerificationCodeChannel;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.repositories.VerificationCodeRepository;
import pe.kerolabs.pozzo.iam.domain.services.CodeGenerationService;
import pe.kerolabs.pozzo.iam.domain.services.CodeMatcher;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.time.Instant;

/**
 * Sends SMS codes to a phone number and checks them, for every flow that has to prove a number:
 * signing in, changing the number of an account and recovering an account.
 */
@Component
class PhoneCodes {

    private final VerificationCodeRepository verificationCodeRepository;
    private final CodeGenerationService codeGenerationService;
    private final VerificationCodeChannel verificationCodeChannel;
    private final TestPhoneNumbers testPhoneNumbers;

    PhoneCodes(VerificationCodeRepository verificationCodeRepository,
               CodeGenerationService codeGenerationService,
               VerificationCodeChannel verificationCodeChannel,
               TestPhoneNumbers testPhoneNumbers) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.codeGenerationService = codeGenerationService;
        this.verificationCodeChannel = verificationCodeChannel;
        this.testPhoneNumbers = testPhoneNumbers;
    }

    /**
     * Sends a new code and invalidates the pending ones for the same number.
     *
     * @return the issued code, or an error when the previous one was sent less than thirty seconds ago
     */
    Result<VerificationCode, ApplicationError> issue(PhoneNumber phoneNumber, Instant now) {
        var latest = verificationCodeRepository.findLatestByPhoneNumber(phoneNumber);
        if (latest.isPresent() && !latest.get().canBeReplacedAt(now)) {
            return Result.failure(ApplicationError.tooManyRequests(
                    "VERIFICATION_CODE_RESEND_TOO_SOON",
                    "A new code can be requested from %s".formatted(latest.get().resendAvailableAt())));
        }

        verificationCodeRepository.findAllPendingByPhoneNumber(phoneNumber).forEach(pending -> {
            pending.invalidate();
            verificationCodeRepository.save(pending);
        });

        // Test numbers get their fixed code and no SMS; the rest go through the configured channel.
        var storedValue = testPhoneNumbers.fixedCodeFor(phoneNumber)
                .map(codeGenerationService::hash)
                .orElseGet(() -> verificationCodeChannel.deliver(phoneNumber));
        return Result.success(verificationCodeRepository.save(VerificationCode.issue(phoneNumber, storedValue, now)));
    }

    /**
     * Checks the code typed for a number. A wrong code uses up one attempt.
     *
     * @return the number when the code is right, or an error when it is wrong, expired, blocked or missing
     */
    Result<PhoneNumber, ApplicationError> verify(PhoneNumber phoneNumber, String code, Instant now) {
        var pending = verificationCodeRepository.findPendingByPhoneNumber(phoneNumber);
        if (pending.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized(
                    "VERIFICATION_CODE_NOT_REQUESTED", "There is no pending code for this phone number"));
        }

        var verificationCode = pending.get();
        var outcome = verificationCode.verify(code, now, matcherFor(verificationCode));
        verificationCodeRepository.save(verificationCode);

        return switch (outcome) {
            case INVALID -> Result.failure(ApplicationError.unauthorized(
                    "INVALID_VERIFICATION_CODE",
                    "%d attempts left".formatted(verificationCode.remainingAttempts())));
            case EXPIRED -> Result.failure(ApplicationError.unauthorized(
                    "EXPIRED_VERIFICATION_CODE", "The code is no longer valid"));
            case BLOCKED -> Result.failure(ApplicationError.unauthorized(
                    "BLOCKED_VERIFICATION_CODE", "The code ran out of attempts"));
            case VERIFIED -> Result.success(phoneNumber);
        };
    }

    private CodeMatcher matcherFor(VerificationCode verificationCode) {
        var phoneNumber = verificationCode.getPhoneNumber();
        if (testPhoneNumbers.fixedCodeFor(phoneNumber).isPresent()) {
            return input -> codeGenerationService.matches(input, verificationCode.getCodeHash());
        }
        return input -> verificationCodeChannel.check(phoneNumber, input, verificationCode.getCodeHash());
    }
}
