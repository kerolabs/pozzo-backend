package pe.kerolabs.pozzo.iam.application.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.application.queryservices.AccountQueryService;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetProfileQuery;
import pe.kerolabs.pozzo.iam.interfaces.acl.IamContextFacade;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements the IAM facade on top of the account query service.
 */
@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final AccountQueryService accountQueryService;

    public IamContextFacadeImpl(AccountQueryService accountQueryService) {
        this.accountQueryService = accountQueryService;
    }

    @Override
    public Optional<String> fetchDisplayNameByAccountId(UUID accountId) {
        return accountQueryService.handle(new GetProfileQuery(accountId))
                .map(account -> account.getProfile().displayName());
    }
}
