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

    public Optional<String> fetchDisplayName(UUID accountId) {
        return iamContextFacade.fetchDisplayNameByAccountId(accountId);
    }

    /**
     * The photo of each member of the group that uses the application and has one, by account.
     */
    public Map<UUID, String> fetchPhotoUrls(SavingsGroup group) {
        var accountIds = group.activeMemberships().stream()
                .map(Membership::getMemberId)
                .filter(Objects::nonNull)
                .toList();
        return iamContextFacade.fetchPhotoUrlsByAccountIds(accountIds);
    }
}
