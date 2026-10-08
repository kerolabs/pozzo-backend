package pe.kerolabs.pozzo.iam.interfaces.acl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * What Identity and Access offers to the other bounded contexts. They call this facade instead of
 * the IAM repositories or aggregates, so the account model can change without breaking them.
 */
public interface IamContextFacade {

    /**
     * Returns the display name of an account, if it exists.
     *
     * @param accountId the account identifier
     * @return the name the member shows to the group
     */
    Optional<String> fetchDisplayNameByAccountId(UUID accountId);

    /**
     * Returns the profile photo of each account that has one.
     *
     * @param accountIds the account identifiers
     * @return the public URL of the photo by account; accounts without photo are left out
     */
    Map<UUID, String> fetchPhotoUrlsByAccountIds(Collection<UUID> accountIds);
}
