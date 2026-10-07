package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Result of a correct code: the session of a registered member, or a registration token for a new one.
 */
@Schema(name = "VerificationResponse",
        description = "Session of a registered member, or a registration token when the number has no account")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerificationResource(
        @Schema(description = "True when the number has no account and the registration must be completed")
        boolean registrationRequired,

        @Schema(description = "Session of the member, when the number already had an account", nullable = true)
        AuthenticatedResource session,

        @Schema(description = "Token to send to /api/v1/auth/register, when the number has no account", nullable = true)
        String registrationToken,

        @Schema(description = "Moment the registration token expires", nullable = true)
        Instant registrationTokenExpiresAt) {
}
