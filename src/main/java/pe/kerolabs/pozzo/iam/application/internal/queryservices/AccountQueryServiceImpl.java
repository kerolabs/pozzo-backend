package pe.kerolabs.pozzo.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.kerolabs.pozzo.iam.application.queryservices.AccountQueryService;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetAccountByPhoneQuery;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetProfileQuery;
import pe.kerolabs.pozzo.iam.domain.model.queries.ValidateTokenQuery;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.SessionTokenClaims;
import pe.kerolabs.pozzo.iam.domain.repositories.AccountRepository;
import pe.kerolabs.pozzo.iam.domain.repositories.SessionRepository;
import pe.kerolabs.pozzo.iam.domain.services.TokenService;

import java.time.Clock;
import java.util.Optional;

/**
 * Resolves account read queries and validates session tokens.
 */
@Service
@Transactional(readOnly = true)
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;
    private final Clock clock;

    public AccountQueryServiceImpl(AccountRepository accountRepository, SessionRepository sessionRepository,
                                   TokenService tokenService, Clock clock) {
        this.accountRepository = accountRepository;
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Override
    public Optional<Account> handle(GetProfileQuery query) {
        return accountRepository.findById(query.accountId());
    }

    @Override
    public Optional<Account> handle(GetAccountByPhoneQuery query) {
        return accountRepository.findByPhoneNumber(query.phoneNumber());
    }

    /**
     * A token is valid when its signature and expiration are correct and the session it names
     * exists, belongs to the same account and has not been revoked.
     */
    @Override
    public Optional<SessionTokenClaims> handle(ValidateTokenQuery query) {
        var now = clock.instant();
        return tokenService.readSessionToken(query.token())
                .filter(claims -> sessionRepository.findByTokenHash(tokenService.hash(query.token()))
                        .filter(session -> session.getId().equals(claims.sessionId()))
                        .filter(session -> session.getAccountId().equals(claims.accountId()))
                        .filter(session -> session.isActive(now))
                        .isPresent());
    }
}
