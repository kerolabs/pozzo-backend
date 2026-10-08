package pe.kerolabs.pozzo.savingsgroups.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.interfaces.acl.IamContextFacade;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption layer towards Identity and Access. Savings Groups copies the name a member shows to
 * the group into the membership when the member joins, and reads the photos when it lists the members.
 */
@Service
public class ExternalIamService {

    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    /**
     * Resolves the profile display name of a member from the IAM context.
     *
     * @param accountId the account identifier
     * @return optional display name, or empty if account not found
     */
    public Optional<String> fetchDisplayName(UUID accountId) {
        return iamContextFacade.fetchDisplayNameByAccountId(accountId);
    }

    /**
     * Retrieves the map of profile avatar photo URLs for all app users in the given savings group.
     *
     * @param group the savings group whose members are queried
     * @return map of account IDs to their profile photo URLs
     */
    public Map<UUID, String> fetchPhotoUrls(SavingsGroup group) {
        var accountIds = group.activeMemberships().stream()
                .map(Membership::getMemberId)
                .filter(Objects::nonNull)
                .toList();
        return iamContextFacade.fetchPhotoUrlsByAccountIds(accountIds);
    }
}
