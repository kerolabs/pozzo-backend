package pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.assemblers;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Profile;
import pe.kerolabs.pozzo.iam.infrastructure.persistence.jpa.entities.AccountPersistenceEntity;

/**
 * Static assembler between the account aggregate and its persistence entity.
 */
public final class AccountPersistenceAssembler {

    private AccountPersistenceAssembler() {
    }

    public static Account toDomainFromPersistence(AccountPersistenceEntity entity) {
        var account = new Account();
        account.restoreState(
                entity.getId(),
                PhoneNumber.fromE164(entity.getPhoneNumber()),
                new Profile(entity.getDisplayName(), entity.getPhotoUrl(), entity.getTheme()),
                entity.getTermsAcceptedAt(),
                entity.getStatus());
        return account;
    }

    /**
     * Copies the state of the aggregate onto a new or already loaded persistence entity.
     */
    public static AccountPersistenceEntity toPersistenceFromDomain(Account account, AccountPersistenceEntity entity) {
        entity.setId(account.getId());
        entity.setPhoneNumber(account.getPhoneNumber().e164());
        entity.setDisplayName(account.getProfile().displayName());
        entity.setPhotoUrl(account.getProfile().photoUrl());
        entity.setTheme(account.getProfile().theme());
        entity.setTermsAcceptedAt(account.getTermsAcceptedAt());
        entity.setStatus(account.getStatus());
        return entity;
    }
}
