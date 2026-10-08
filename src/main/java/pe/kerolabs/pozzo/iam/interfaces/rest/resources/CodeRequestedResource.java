package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * A verification code was sent.
 */
@Schema(name = "CodeRequestedResponse", description = "A verification code was sent by SMS")
public record CodeRequestedResource(
        @Schema(description = "Number the code was sent to, in E.164 format", example = "+51999000123")
        String phoneNumber,

        @Schema(description = "Moment the code stops being valid")
        Instant expiresAt,

        @Schema(description = "Moment from which a new code may be requested")
        Instant resendAvailableAt) {
}
