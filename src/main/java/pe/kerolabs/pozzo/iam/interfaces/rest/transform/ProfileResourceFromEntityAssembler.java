package pe.kerolabs.pozzo.iam.interfaces.rest.transform;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.ProfileResource;

/**
 * Converts the {@link Account} aggregate into {@link ProfileResource}.
 */
public class ProfileResourceFromEntityAssembler {

    public static ProfileResource toResourceFromEntity(Account account) {
        return new ProfileResource(
                account.getId(),
                account.getPhoneNumber().e164(),
                account.getProfile().displayName(),
                account.getProfile().photoUrl(),
                account.getProfile().theme());
    }
}
