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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.application.commandservices.AuthenticationCommandService;
import pe.kerolabs.pozzo.iam.domain.model.commands.SignOutCommand;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.AuthenticatedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.CodeRequestedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RegisterResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RequestCodeResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.VerificationResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.VerifyCodeResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.AuthenticatedResourceFromResultAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.CodeRequestedResourceFromEntityAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.CompleteRegistrationCommandFromResourceAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.RequestCodeCommandFromResourceAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.VerifyCodeCommandFromResourceAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * Passwordless access with the phone number: request a code, verify it, complete the registration
 * and sign out.
 */
@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Passwordless access with the phone number")
public class AuthenticationController {

    private final AuthenticationCommandService authenticationCommandService;

    public AuthenticationController(AuthenticationCommandService authenticationCommandService) {
        this.authenticationCommandService = authenticationCommandService;
    }

    /**
     * Requests a 6-digit SMS verification code to begin login or registration.
     * Code expires in 10 minutes and resending is rate-limited to 30 seconds.
     *
     * @param resource payload containing the target mobile phone number
     * @return 202 Accepted with code dispatch information, or error status
     */
    @PostMapping("/codes")
    @SecurityRequirements
    @Operation(summary = "Request a verification code",
            description = "Sends a six-digit code by SMS. The code is valid for 10 minutes; a new one can be "
                    + "requested after 30 seconds and replaces the previous one.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Code sent",
                    content = @Content(schema = @Schema(implementation = CodeRequestedResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid phone number",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "429", description = "A code was requested less than 30 seconds ago",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> requestCode(@Valid @RequestBody RequestCodeResource resource) {
        var result = authenticationCommandService.handle(
                RequestCodeCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, CodeRequestedResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.ACCEPTED);
    }

    /**
     * Verifies the SMS code. If the phone is already associated with an account, opens a session;
     * otherwise, returns a short-lived registration token to proceed with profile creation.
     *
     * @param resource payload with phone number and submitted 6-digit code
     * @return 200 OK with session credentials or registration token
     */
    @PostMapping("/codes/verify")
    @SecurityRequirements
    @Operation(summary = "Verify a code",
            description = "Opens a session when the number has an account. Otherwise returns a registration "
                    + "token, valid for 15 minutes, to complete the registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code verified",
                    content = @Content(schema = @Schema(implementation = VerificationResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid phone number or code format",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401",
                    description = "Wrong, expired or blocked code, or no code requested for the number",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "The account is deactivated",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeResource resource) {
        var result = authenticationCommandService.handle(
                VerifyCodeCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AuthenticatedResourceFromResultAssembler::toResourceFromResult, HttpStatus.OK);
    }

    /**
     * Completes profile registration using the registration token obtained from SMS verification.
     * Creates the member account and opens their initial session.
     *
     * @param resource payload containing registration token, full name, accepted terms, and optional profile details
     * @return 201 Created with authenticated member session and tokens
     */
    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Complete the registration",
            description = "Creates the account of a verified number and opens its first session.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created and session opened",
                    content = @Content(schema = @Schema(implementation = AuthenticatedResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired registration token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "The number already has an account",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The terms were not accepted",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterResource resource) {
        var result = authenticationCommandService.handle(
                CompleteRegistrationCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, AuthenticatedResourceFromResultAssembler::toResourceFromResult, HttpStatus.CREATED);
    }

    /**
     * Signs out the authenticated member and revokes the active session token.
     *
     * @param member the authenticated member making the request
     * @return 204 No Content upon successful revocation
     */
    @PostMapping("/sign-out")
    @Operation(summary = "Sign out", description = "Revokes the session the request was made with.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session revoked", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> signOut(@AuthenticationPrincipal AuthenticatedMember member) {
        var result = authenticationCommandService.handle(new SignOutCommand(member.accountId(), member.sessionId()));
        return ResponseEntityAssembler.toNoContentResponseEntityFromResult(result);
    }
}
