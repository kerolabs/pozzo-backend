package pe.kerolabs.pozzo.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticationCommandService;
import pe.kerolabs.pozzo.iam.domain.model.commands.RecoverAccountCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryPhoneCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyRecoveryCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.AuthenticatedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.CodeRequestedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RecoveryResources;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.AuthenticatedResourceFromResultAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.CodeRequestedResourceFromEntityAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * Recovery of an account whose phone number was lost: prove it with a code sent to the backup email,
 * then link a new number with an SMS code.
 */
@RestController
@RequestMapping(value = "/api/v1/auth/recovery", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Account recovery", description = "Recover an account with the backup email and a new number")
public class AccountRecoveryController {

    private final AuthenticationCommandService authenticationCommandService;

    public AccountRecoveryController(AuthenticationCommandService authenticationCommandService) {
        this.authenticationCommandService = authenticationCommandService;
    }

    @PostMapping("/codes")
    @SecurityRequirements
    @Operation(summary = "Request a recovery code",
            description = "Sends a six-digit code to the backup email when an active account has it. The answer "
                    + "is the same when no account has it, so registered emails cannot be found out.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted",
                    content = @Content(schema = @Schema(implementation = RecoveryResources.RecoveryCodeRequested.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "429", description = "A code was requested less than 30 seconds ago",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> requestCode(@Valid @RequestBody RecoveryResources.RequestRecoveryCode resource) {
        var result = authenticationCommandService.handle(new RequestRecoveryCodeCommand(resource.email()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                request -> new RecoveryResources.RecoveryCodeRequested(
                        request.email(), request.expiresAt(), request.resendAvailableAt()),
                HttpStatus.ACCEPTED);
    }

    @PostMapping("/codes/verify")
    @SecurityRequirements
    @Operation(summary = "Verify a recovery code", description = "Returns a token, valid for 15 minutes, to link a new number.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account proved",
                    content = @Content(schema = @Schema(implementation = RecoveryResources.RecoveryToken.class))),
            @ApiResponse(responseCode = "401", description = "Wrong, expired or blocked code, or none requested",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> verifyCode(@Valid @RequestBody RecoveryResources.VerifyRecoveryCode resource) {
        var result = authenticationCommandService.handle(
                new VerifyRecoveryCodeCommand(resource.email(), resource.code()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                verification -> new RecoveryResources.RecoveryToken(
                        verification.recoveryToken(), verification.expiresAt()),
                HttpStatus.OK);
    }

    @PostMapping("/phone-number/codes")
    @SecurityRequirements
    @Operation(summary = "Request an SMS code for the new number")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Code sent",
                    content = @Content(schema = @Schema(implementation = CodeRequestedResource.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired recovery token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The number belongs to another account",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> requestPhoneCode(@Valid @RequestBody RecoveryResources.RequestRecoveryPhoneCode resource) {
        var result = authenticationCommandService.handle(new RequestRecoveryPhoneCodeCommand(
                resource.recoveryToken(), PhoneNumber.ofPeruvianMobile(resource.phoneNumber())));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, CodeRequestedResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.ACCEPTED);
    }

    @PostMapping("/phone-number")
    @SecurityRequirements
    @Operation(summary = "Link the new number and sign in",
            description = "Links the verified number to the account, closes the sessions of the lost phone and "
                    + "opens a new one. Groups and history stay with the account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account recovered and session opened",
                    content = @Content(schema = @Schema(implementation = AuthenticatedResource.class))),
            @ApiResponse(responseCode = "401", description = "Invalid recovery token or SMS code",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The number belongs to another account",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> recover(@Valid @RequestBody RecoveryResources.RecoverAccount resource) {
        var result = authenticationCommandService.handle(new RecoverAccountCommand(
                resource.recoveryToken(), PhoneNumber.ofPeruvianMobile(resource.phoneNumber()),
                resource.code(), resource.deviceLabel()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AuthenticatedResourceFromResultAssembler::toResourceFromResult, HttpStatus.OK);
    }
}
