package pe.kerolabs.pozzo.iam.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.iam.domain.model.events.AccountCreatedEvent;
import pe.kerolabs.pozzo.iam.domain.model.events.ProfileUpdatedEvent;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.AccountStatus;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * Account aggregate root: a member identified by a unique mobile number, with a profile
 * and the moment the Terms and Conditions and the Privacy Policy were accepted.
 */
@Getter
public class Account extends AbstractDomainAggregateRoot<Account> {

    private UUID id;
    private PhoneNumber phoneNumber;
    private Profile profile;
    private Instant termsAcceptedAt;
    private AccountStatus status;

    public Account() {
    }

    /**
     * Registers a member whose phone number was just verified.
     *
     * @param phoneNumber   the verified phone number
     * @param profile       the initial profile
     * @param termsAccepted whether the member accepted the terms; registration is not possible otherwise
     * @param now           the current time
     * @return the new account
     */
    public static Account register(PhoneNumber phoneNumber, Profile profile, boolean termsAccepted, Instant now) {
        if (!termsAccepted) {
            throw new IllegalArgumentException("The Terms and Conditions and the Privacy Policy must be accepted");
        }
        var account = new Account();
        account.id = UUID.randomUUID();
        account.phoneNumber = phoneNumber;
        account.profile = profile;
        account.termsAcceptedAt = now;
        account.status = AccountStatus.ACTIVE;
        account.registerDomainEvent(new AccountCreatedEvent(
                account.id, phoneNumber.e164(), profile.displayName(), now));
        return account;
    }

    /**
     * Replaces the profile.
     */
    public void updateProfile(Profile profile, Instant now) {
        this.profile = profile;
        registerDomainEvent(new ProfileUpdatedEvent(id, profile.displayName(), now));
    }

    public boolean hasAcceptedTerms() {
        return termsAcceptedAt != null;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    /**
     * Deactivates the account; the member can no longer sign in.
     */
    public void deactivate() {
        this.status = AccountStatus.DEACTIVATED;
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, PhoneNumber phoneNumber, Profile profile, Instant termsAcceptedAt,
                             AccountStatus status) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.profile = profile;
        this.termsAcceptedAt = termsAcceptedAt;
        this.status = status;
    }
}
