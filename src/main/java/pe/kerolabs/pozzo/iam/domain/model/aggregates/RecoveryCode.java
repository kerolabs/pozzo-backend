package pe.kerolabs.pozzo.iam.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.iam.domain.model.events.RecoveryCodeRequestedEvent;
import pe.kerolabs.pozzo.iam.domain.model.events.RecoveryCodeVerifiedEvent;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationOutcome;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.domain.services.CodeMatcher;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * RecoveryCode aggregate root: a six-digit code sent to the backup email of an account, so a member
 * who lost their phone number can prove the account is theirs and link a new number.
 *
 * <p>It follows the rules of {@link VerificationCode}: valid for ten minutes, three attempts, and a
 * new one only after thirty seconds.</p>
 */
@Getter
public class RecoveryCode extends AbstractDomainAggregateRoot<RecoveryCode> {

    public static final Duration VALIDITY = VerificationCode.VALIDITY;
    public static final Duration RESEND_COOLDOWN = VerificationCode.RESEND_COOLDOWN;
    public static final int MAX_ATTEMPTS = VerificationCode.MAX_ATTEMPTS;

    private UUID id;
    private UUID accountId;
    private String email;
    private String codeHash;
    private Instant issuedAt;
    private Instant expiresAt;
    private int attempts;
    private VerificationStatus status;

    public RecoveryCode() {
    }

    /**
     * Issues a code for the backup email of an account.
     *
     * @param accountId the account to recover
     * @param email     the backup email the code is sent to
     * @param codeHash  the hash of the code; the code itself is never stored
     * @param now       the current time
     * @return the new pending code
     */
    public static RecoveryCode issue(UUID accountId, String email, String codeHash, Instant now) {
        var code = new RecoveryCode();
        code.id = UUID.randomUUID();
        code.accountId = accountId;
        code.email = email;
        code.codeHash = codeHash;
        code.issuedAt = now;
        code.expiresAt = now.plus(VALIDITY);
        code.attempts = 0;
        code.status = VerificationStatus.PENDING;
        code.registerDomainEvent(new RecoveryCodeRequestedEvent(code.id, accountId, now));
        return code;
    }

    /**
     * Checks the code typed by the member. A wrong code uses up one attempt; the third wrong
     * attempt blocks the code, and a new one has to be requested.
     */
    public VerificationOutcome verify(String input, Instant now, CodeMatcher matcher) {
        if (status == VerificationStatus.BLOCKED) {
            return VerificationOutcome.BLOCKED;
        }
        if (status != VerificationStatus.PENDING) {
            return VerificationOutcome.EXPIRED;
        }
        if (!now.isBefore(expiresAt)) {
            status = VerificationStatus.EXPIRED;
            return VerificationOutcome.EXPIRED;
        }
        if (matcher.matches(input)) {
            status = VerificationStatus.VERIFIED;
            registerDomainEvent(new RecoveryCodeVerifiedEvent(id, accountId, now));
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

    public int remainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - attempts);
    }

    public Instant resendAvailableAt() {
        return issuedAt.plus(RESEND_COOLDOWN);
    }

    public boolean canBeReplacedAt(Instant now) {
        return !now.isBefore(resendAvailableAt());
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID accountId, String email, String codeHash, Instant issuedAt,
                             Instant expiresAt, int attempts, VerificationStatus status) {
        this.id = id;
        this.accountId = accountId;
        this.email = email;
        this.codeHash = codeHash;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.attempts = attempts;
        this.status = status;
    }
}
