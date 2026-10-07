package pe.kerolabs.pozzo.savingsgroups.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.interfaces.acl.IamContextFacade;

import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption layer towards Identity and Access. Savings Groups only needs the name a member
 * shows to the group, which it copies into the membership when the member joins.
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
}
