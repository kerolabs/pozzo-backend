package pe.kerolabs.pozzo.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.kerolabs.pozzo.iam.application.queryservices.AccountQueryService;
import pe.kerolabs.pozzo.iam.domain.model.queries.ValidateTokenQuery;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;

import java.io.IOException;
import java.util.List;

/**
 * Reads the bearer token of each request, validates it with {@link AccountQueryService} and,
 * when it is valid, places the {@link AuthenticatedMember} in the security context.
 *
 * <p>A request without a valid token goes on unauthenticated; the security configuration
 * decides whether the route requires authentication.</p>
 */
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final List<SimpleGrantedAuthority> MEMBER_AUTHORITIES =
            List.of(new SimpleGrantedAuthority("ROLE_MEMBER"));

    private final AccountQueryService accountQueryService;

    public BearerAuthorizationRequestFilter(AccountQueryService accountQueryService) {
        this.accountQueryService = accountQueryService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            var token = header.substring(BEARER_PREFIX.length()).trim();
            accountQueryService.handle(new ValidateTokenQuery(token)).ifPresent(claims -> {
                var principal = new AuthenticatedMember(claims.accountId(), claims.sessionId());
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, MEMBER_AUTHORITIES);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        filterChain.doFilter(request, response);
    }
}
