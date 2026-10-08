package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Requests and responses of the account recovery with the backup email, and of the change of number.
 */
public final class RecoveryResources {

    private RecoveryResources() {
    }

    @Schema(name = "RequestRecoveryCodeRequest", description = "Request to send a recovery code to the backup email")
    public record RequestRecoveryCode(
            @Schema(description = "Backup email registered in the profile", example = "anna@ejemplo.pe")
            @NotBlank @Email @Size(max = 120)
            String email) {
    }

    @Schema(name = "RecoveryCodeRequestedResponse",
            description = "Sent when the email belongs to an account; the answer is the same either way")
    public record RecoveryCodeRequested(
            @Schema(description = "Email, in lower case", example = "anna@ejemplo.pe") String email,
            @Schema(description = "Moment the code stops being valid") Instant expiresAt,
            @Schema(description = "Moment from which a new code may be requested") Instant resendAvailableAt) {
    }

    @Schema(name = "VerifyRecoveryCodeRequest", description = "Request to verify the code received by email")
    public record VerifyRecoveryCode(
            @Schema(description = "Backup email the code was sent to", example = "anna@ejemplo.pe")
            @NotBlank @Email @Size(max = 120)
            String email,
            @Schema(description = "Six-digit code received by email", example = "482913")
            @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must have six digits")
            String code) {
    }

    @Schema(name = "RecoveryTokenResponse", description = "The account was proved; a new number can be linked")
    public record RecoveryToken(
            @Schema(description = "Token to link a new number, valid for 15 minutes") String recoveryToken,
            @Schema(description = "Moment the token expires") Instant expiresAt) {
    }

    @Schema(name = "RequestRecoveryPhoneCodeRequest", description = "Request to send an SMS code to the new number")
    public record RequestRecoveryPhoneCode(
            @Schema(description = "Token received after verifying the email") @NotBlank String recoveryToken,
            @Schema(description = "New Peruvian mobile number", example = "999000124")
            @NotBlank @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
            String phoneNumber) {
    }

    @Schema(name = "RecoverAccountRequest", description = "Request to link the new number and sign in")
    public record RecoverAccount(
            @Schema(description = "Token received after verifying the email") @NotBlank String recoveryToken,
            @Schema(description = "New Peruvian mobile number", example = "999000124")
            @NotBlank @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
            String phoneNumber,
            @Schema(description = "Six-digit code received by SMS at the new number", example = "482913")
            @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must have six digits")
            String code,
            @Schema(description = "Name of the device", example = "Pixel 8", nullable = true)
            @Size(max = 80)
            String deviceLabel) {
    }

    @Schema(name = "RequestPhoneChangeCodeRequest", description = "Request to send an SMS code to the new number")
    public record RequestPhoneChangeCode(
            @Schema(description = "New Peruvian mobile number", example = "999000124")
            @NotBlank @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
            String phoneNumber) {
    }

    @Schema(name = "ChangePhoneNumberRequest", description = "Request to switch the account to the new number")
    public record ChangePhoneNumber(
            @Schema(description = "New Peruvian mobile number", example = "999000124")
            @NotBlank @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
            String phoneNumber,
            @Schema(description = "Six-digit code received by SMS at the new number", example = "482913")
            @NotBlank @Pattern(regexp = "^\\d{6}$", message = "must have six digits")
            String code) {
    }
}
