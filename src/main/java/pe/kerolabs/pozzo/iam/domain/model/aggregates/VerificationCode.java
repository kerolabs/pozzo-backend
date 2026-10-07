package pe.kerolabs.pozzo.iam.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.iam.domain.model.events.CodeRequestedEvent;
import pe.kerolabs.pozzo.iam.domain.model.events.CodeVerifiedEvent;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationOutcome;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.domain.services.CodeMatcher;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * VerificationCode aggregate root: a six-digit code sent by SMS to prove that a phone number
 * belongs to whoever requested it.
 *
 * <p>It is a separate aggregate from {@link Account} because it exists before the account does.
 * A code is valid for ten minutes, admits three attempts and can be replaced by a new one only
 * after thirty seconds.</p>
 */
@Getter
public class VerificationCode extends AbstractDomainAggregateRoot<VerificationCode> {

    public static final Duration VALIDITY = Duration.ofMinutes(10);
    public static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);
    public static final int MAX_ATTEMPTS = 3;

    private UUID id;
    private PhoneNumber phoneNumber;
    private String codeHash;
    private Instant issuedAt;
    private Instant expiresAt;
    private int attempts;
    private VerificationStatus status;

    public VerificationCode() {
    }

    /**
     * Issues a code for a phone number.
     *
     * @param phoneNumber the number the code is sent to
     * @param codeHash    the hash of the code, or a marker when an external service keeps the code;
     *                    the code itself is never stored
     * @param now         the current time
     * @return the new pending code
     */
    public static VerificationCode issue(PhoneNumber phoneNumber, String codeHash, Instant now) {
        var code = new VerificationCode();
        code.id = UUID.randomUUID();
        code.phoneNumber = phoneNumber;
        code.codeHash = codeHash;
        code.issuedAt = now;
        code.expiresAt = now.plus(VALIDITY);
        code.attempts = 0;
        code.status = VerificationStatus.PENDING;
        code.registerDomainEvent(new CodeRequestedEvent(code.id, phoneNumber.e164(), now));
        return code;
    }

    /**
     * Checks the code typed by the member. A wrong code uses up one attempt; the third wrong
     * attempt blocks the code, and a new one has to be requested.
     *
     * @param input   the code typed by the member
     * @param now     the current time
     * @param matcher decides whether the input is the code that was sent
     * @return the outcome of the attempt
     */
    public VerificationOutcome verify(String input, Instant now, CodeMatcher matcher) {
        if (status == VerificationStatus.BLOCKED) {
            return VerificationOutcome.BLOCKED;
        }
        if (status != VerificationStatus.PENDING) {
            return VerificationOutcome.EXPIRED;
        }
        if (isExpired(now)) {
            status = VerificationStatus.EXPIRED;
            return VerificationOutcome.EXPIRED;
        }
        if (matcher.matches(input)) {
            status = VerificationStatus.VERIFIED;
            registerDomainEvent(new CodeVerifiedEvent(id, phoneNumber.e164(), now));
            return VerificationOutcome.VERIFIED;
        }
        attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            status = VerificationStatus.BLOCKED;
            return VerificationOutcome.BLOCKED;
        }
        return VerificationOutcome.INVALID;
    }

    /**
     * Invalidates a pending code because the member asked for a new one.
     */
    public void invalidate() {
        if (status == VerificationStatus.PENDING) {
            status = VerificationStatus.EXPIRED;
        }
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public int remainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    /**
     * The moment from which a new code may be requested for the same number.
     */
    public Instant resendAvailableAt() {
        return issuedAt.plus(RESEND_COOLDOWN);
    }

    public boolean canBeReplacedAt(Instant now) {
        return !now.isBefore(resendAvailableAt());
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, PhoneNumber phoneNumber, String codeHash, Instant issuedAt, Instant expiresAt,
                             int attempts, VerificationStatus status) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.status = status;
    }
}
