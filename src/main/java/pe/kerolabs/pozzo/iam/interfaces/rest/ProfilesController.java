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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.kerolabs.pozzo.iam.application.commandservices.AccountCommandService;
import pe.kerolabs.pozzo.iam.application.queryservices.AccountQueryService;
import pe.kerolabs.pozzo.iam.domain.model.commands.ChangeProfilePhotoCommand;
import pe.kerolabs.pozzo.iam.domain.model.queries.GetProfileQuery;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.ProfileResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.resources.UpdateProfileResource;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.ProfileResourceFromEntityAssembler;
import pe.kerolabs.pozzo.iam.interfaces.rest.transform.UpdateProfileCommandFromResourceAssembler;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.io.IOException;

/**
 * Profile, photo and visual theme of the authenticated member.
 */
@RestController
@RequestMapping(value = "/api/v1/members/me/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Profiles", description = "Profile of the authenticated member")
public class ProfilesController {

    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    public ProfilesController(AccountCommandService accountCommandService, AccountQueryService accountQueryService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
    }

    @GetMapping
    @Operation(summary = "Get my profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile found",
                    content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal AuthenticatedMember member) {
        return accountQueryService.handle(new GetProfileQuery(member.accountId()))
                .<ResponseEntity<?>>map(account ->
                        ResponseEntity.ok(ProfileResourceFromEntityAssembler.toResourceFromEntity(account)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Account", member.accountId().toString())));
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Change my photo",
            description = "Stores a JPEG, PNG or WebP image of up to 2 MB as the profile photo and deletes the previous one.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photo changed",
                    content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "400", description = "Not an image, or too large",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "503", description = "The photo storage did not answer or is not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> changePhoto(@AuthenticationPrincipal AuthenticatedMember member,
                                         @RequestPart("photo") MultipartFile photo) throws IOException {
        var result = accountCommandService.handle(
                new ChangeProfilePhotoCommand(member.accountId(), photo.getBytes(), photo.getContentType()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProfileResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/photo")
    @Operation(summary = "Remove my photo", description = "The initials are shown instead.")
    @ApiResponse(responseCode = "200", description = "Photo removed",
            content = @Content(schema = @Schema(implementation = ProfileResource.class)))
    public ResponseEntity<?> removePhoto(@AuthenticationPrincipal AuthenticatedMember member) {
        var result = accountCommandService.handle(ChangeProfilePhotoCommand.remove(member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProfileResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PutMapping
    @Operation(summary = "Update my profile", description = "Changes the display name, photo, visual theme, Yape or Plin number and backup email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = ProfileResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal AuthenticatedMember member,
                                           @Valid @RequestBody UpdateProfileResource resource) {
        var result = accountCommandService.handle(
                UpdateProfileCommandFromResourceAssembler.toCommandFromResource(member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ProfileResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
