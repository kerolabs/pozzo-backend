package pe.kerolabs.pozzo.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.iam.application.commandservices.AccountCommandService;
import pe.kerolabs.pozzo.iam.domain.model.commands.ChangePhoneNumberCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestPhoneChangeCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.CodeRequestedResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.ProfileResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.RecoveryResources;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.CodeRequestedResourceFromEntityAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

/**
 * Change of the phone number of a signed-in member, confirmed with an SMS code sent to the new number.
 */
@RestController
@RequestMapping(value = "/api/v1/members/me/phone-number", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile of the authenticated member")
public class PhoneNumberController {

    private final AccountCommandService accountCommandService;

    public PhoneNumberController(AccountCommandService accountCommandService) {
        this.accountCommandService = accountCommandService;
    }

    @PostMapping("/codes")
    @Operation(summary = "Request a code for the new number", description = "Sends an SMS code to the new number.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Code sent",
                    content = @Content(schema = @Schema(implementation = CodeRequestedResource.class))),
            @ApiResponse(responseCode = "422", description = "The number is the current one or belongs to another account",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "429", description = "A code was requested less than 30 seconds ago",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> requestCode(@AuthenticationPrincipal AuthenticatedMember member,
                                         @Valid @RequestBody RecoveryResources.RequestPhoneChangeCode resource) {
        var result = accountCommandService.handle(new RequestPhoneChangeCodeCommand(
                member.accountId(), PhoneNumber.ofPeruvianMobile(resource.phoneNumber())));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, CodeRequestedResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.ACCEPTED);
    }

    @PutMapping
    @Operation(summary = "Change my phone number",
            description = "Links the account to the new number once its code is verified. Groups and history stay.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number changed",
                    content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "401", description = "Wrong, expired or blocked code, or none requested",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The number is the current one or belongs to another account",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> changePhoneNumber(@AuthenticationPrincipal AuthenticatedMember member,
                                               @Valid @RequestBody RecoveryResources.ChangePhoneNumber resource) {
        var result = accountCommandService.handle(new ChangePhoneNumberCommand(
                member.accountId(), PhoneNumber.ofPeruvianMobile(resource.phoneNumber()), resource.code()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProfileResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
