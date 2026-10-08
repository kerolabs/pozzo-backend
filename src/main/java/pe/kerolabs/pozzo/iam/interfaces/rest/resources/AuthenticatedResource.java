package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * A member signed in: the session token and the profile.
 */
@Schema(name = "AuthenticatedResponse", description = "Session token and profile of a signed-in member")
public record AuthenticatedResource(
        @Schema(description = "Session token to send as 'Authorization: Bearer <token>'")
        String token,

        @Schema(description = "Moment the session ends if the member does not sign out first")
        Instant expiresAt,

        @Schema(description = "Profile of the member")
        ProfileResource profile) {
}
