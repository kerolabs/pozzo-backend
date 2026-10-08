package pe.kerolabs.pozzo.contributions.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.kerolabs.pozzo.contributions.application.commandservices.ContributionCommandService;
import pe.kerolabs.pozzo.contributions.application.queryservices.ContributionQueryService;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Contribution;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetCycleByIdQuery;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachReceiptImageCommand;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPendingReviewsQuery;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetReceiptImageQuery;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ReceiptImageResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterCashContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.RegisterCoverageResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ReviewContributionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.ContributionCommandFromResourceAssembler;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.ContributionResourceFromEntityAssembler;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.io.IOException;
import java.util.UUID;

/**
 * Registration and review of contributions: with a receipt by the member, in cash or as a coverage
 * by the organizer, and the organizer's review of the receipts that did not match.
 */
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Contributions", description = "Registration and review of contributions")
public class ContributionsController {

    private final ContributionCommandService contributionCommandService;
    private final ContributionQueryService contributionQueryService;

    public ContributionsController(ContributionCommandService contributionCommandService,
                                   ContributionQueryService contributionQueryService) {
        this.contributionCommandService = contributionCommandService;
        this.contributionQueryService = contributionQueryService;
    }

    @PostMapping("/periods/{periodId}/contributions")
    @Operation(summary = "Register my contribution",
            description = "With the data read from the receipt. A receipt that matches the amount, the recipient "
                    + "and the cutoff date is validated at once; otherwise it waits for the organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered: VALIDATED or INCONSISTENT",
                    content = @Content(schema = @Schema(implementation = ContributionResource.class))),
            @ApiResponse(responseCode = "404", description = "The period does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "The receipt was already used in the group",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422",
                    description = "Already settled, waiting for review, or the period no longer accepts contributions",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerContribution(@AuthenticationPrincipal AuthenticatedMember member,
                                                  @PathVariable UUID periodId,
                                                  @Valid @RequestBody RegisterContributionResource resource) {
        var result = contributionCommandService.handle(
                ContributionCommandFromResourceAssembler.toCommandFromResource(periodId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, contribution -> toResource(contribution, member), HttpStatus.CREATED);
    }

    @PostMapping("/periods/{periodId}/contributions/cash")
    @Operation(summary = "Register a cash contribution", description = "Only the organizer, for any member.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registered and validated",
                    content = @Content(schema = @Schema(implementation = ContributionResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Already settled or a different amount",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerCash(@AuthenticationPrincipal AuthenticatedMember member,
                                          @PathVariable UUID periodId,
                                          @Valid @RequestBody RegisterCashContributionResource resource) {
        var result = contributionCommandService.handle(
                ContributionCommandFromResourceAssembler.toCommandFromResource(periodId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, contribution -> toResource(contribution, member), HttpStatus.CREATED);
    }

    @PostMapping("/periods/{periodId}/contributions/coverage")
    @Operation(summary = "Register a coverage",
            description = "Only the organizer: a member puts the money of another member, who then owes it.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coverage registered",
                    content = @Content(schema = @Schema(implementation = ContributionResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Already settled, or a member covering themselves",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerCoverage(@AuthenticationPrincipal AuthenticatedMember member,
                                              @PathVariable UUID periodId,
                                              @Valid @RequestBody RegisterCoverageResource resource) {
        var result = contributionCommandService.handle(
                ContributionCommandFromResourceAssembler.toCommandFromResource(periodId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, contribution -> toResource(contribution, member), HttpStatus.CREATED);
    }

    @PatchMapping("/contributions/{contributionId}/review")
    @Operation(summary = "Review a contribution",
            description = "Only the organizer. Approving settles the member's contribution; rejecting lets the "
                    + "member register it again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviewed",
                    content = @Content(schema = @Schema(implementation = ContributionResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The contribution does not exist",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The contribution is not under review",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> reviewContribution(@AuthenticationPrincipal AuthenticatedMember member,
                                                @PathVariable UUID contributionId,
                                                @Valid @RequestBody ReviewContributionResource resource) {
        var result = contributionCommandService.handle(ContributionCommandFromResourceAssembler
                .toCommandFromResource(contributionId, member.accountId(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, contribution -> toResource(contribution, member), HttpStatus.OK);
    }

    @PutMapping(value = "/contributions/{contributionId}/receipt-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Keep the image of my receipt",
            description = "Stores a JPEG, PNG or WebP image of up to 2 MB in a private storage, so the member and "
                    + "the organizer can see it later. Only the member who registered the contribution.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image kept",
                    content = @Content(schema = @Schema(implementation = ContributionResource.class))),
            @ApiResponse(responseCode = "400", description = "Not an image, or too large",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The contribution does not exist or is not the requester's",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The contribution was not made by transfer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "503", description = "The receipt storage did not answer or is not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> attachReceiptImage(@AuthenticationPrincipal AuthenticatedMember member,
                                                @PathVariable UUID contributionId,
                                                @RequestPart("image") MultipartFile image) throws IOException {
        var result = contributionCommandService.handle(new AttachReceiptImageCommand(
                contributionId, member.accountId(), image.getBytes(), image.getContentType()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, contribution -> toResource(contribution, member), HttpStatus.OK);
    }

    @GetMapping("/contributions/{contributionId}/receipt-image")
    @Operation(summary = "See the image of a receipt",
            description = "A signed link that works for 15 minutes. Only the member the contribution counts for "
                    + "and the organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Temporary link",
                    content = @Content(schema = @Schema(implementation = ReceiptImageResource.class))),
            @ApiResponse(responseCode = "404", description = "No image, or the requester may not see it",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "503", description = "The receipt storage did not answer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getReceiptImage(@AuthenticationPrincipal AuthenticatedMember member,
                                             @PathVariable UUID contributionId) {
        return contributionQueryService.handle(new GetReceiptImageQuery(contributionId, member.accountId()))
                .<ResponseEntity<?>>map(link -> ResponseEntity.ok(new ReceiptImageResource(link.url(), link.expiresAt())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("ReceiptImage", contributionId.toString())));
    }

    @GetMapping("/periods/{periodId}/contributions/pending-review")
    @Operation(summary = "List the contributions to review", description = "Only the organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contributions waiting for review",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContributionResource.class)))),
            @ApiResponse(responseCode = "404", description = "The period does not exist or the requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getPendingReviews(@AuthenticationPrincipal AuthenticatedMember member,
                                               @PathVariable UUID periodId) {
        return contributionQueryService.handle(new GetPendingReviewsQuery(periodId, member.accountId()))
                .<ResponseEntity<?>>map(view -> ResponseEntity.ok(view.contributions().stream()
                        .map(contribution -> ContributionResourceFromEntityAssembler.toResourceFromEntity(
                                contribution, view.cycle()))
                        .toList()))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Period", periodId.toString())));
    }

    private ContributionResource toResource(Contribution contribution, AuthenticatedMember member) {
        var cycle = contributionQueryService.handle(new GetCycleByIdQuery(contribution.getCycleId(), member.accountId()))
                .orElseThrow();
        return ContributionResourceFromEntityAssembler.toResourceFromEntity(contribution, cycle);
    }
}
