package pe.kerolabs.pozzo.iam.interfaces.acl;

import java.util.UUID;

/**
 * Identity of the member who made the current request, placed in the security context by
 * Identity and Access. The controllers of every bounded context read it as the principal:
 *
 * <pre>{@code
 * public ResponseEntity<?> handle(@AuthenticationPrincipal AuthenticatedMember member) { ... }
 * }</pre>
 *
 * @param accountId the account of the member
 * @param sessionId the session the request was made with
 */
public record AuthenticatedMember(UUID accountId, UUID sessionId) {
}
