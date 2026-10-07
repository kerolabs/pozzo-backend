package pe.kerolabs.pozzo.iam.application.queryservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetAccountByPhoneQuery;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetProfileQuery;
import pe.kerolabs.pozzo.iam.domain.model.queries.ValidateTokenQuery;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;

import java.util.Optional;

/**
 * Application service contract for account read queries.
 */
public interface AccountQueryService {

    Optional<Account> handle(GetProfileQuery query);

    Optional<Account> handle(GetAccountByPhoneQuery query);

    /**
     * Validates a session token. Used by the security filter on every request.
     *
     * @return who the token belongs to, when the token is valid and its session is still active
     */
    Optional<SessionTokenClaims> handle(ValidateTokenQuery query);
}
