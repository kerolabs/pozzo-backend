package pe.kerolabs.pozzo.iam.interfaces.acl;

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
}
