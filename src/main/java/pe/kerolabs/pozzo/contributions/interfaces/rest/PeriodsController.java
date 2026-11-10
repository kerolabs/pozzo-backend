package pe.kerolabs.pozzo.contributions.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.application.queryservices.ContributionQueryService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.AttachPayoutProofCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.DeliverPotCommand;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetPayoutProofQuery;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PayoutProofResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PotDeliveryResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.ReceiptImageResource;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.io.IOException;
import java.util.UUID;

/**
 * Delivery of the pot of a period, which moves the cycle to its next turn or closes it.
 */
@RestController
@RequestMapping(value = "/api/v1/periods/{periodId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Periods", description = "Delivery of the pot")
public class PeriodsController {

    private final CycleCommandService cycleCommandService;
    private final ContributionQueryService contributionQueryService;

    public PeriodsController(CycleCommandService cycleCommandService,
                             ContributionQueryService contributionQueryService) {
        this.cycleCommandService = cycleCommandService;
        this.contributionQueryService = contributionQueryService;
    }

    /**
     * Confirms the distribution and delivery of the gathered pot to the designated turn beneficiary.
     * Transitions the cycle to the next period or closes the cycle upon completing all turns.
     * Only permitted for the group organizer.
     *
     * @param member the authenticated organizer
     * @param periodId the identifier of the period whose pot is delivered
     * @return 200 OK with pot delivery details, or error status
     */
    @PostMapping("/payout")
    @Operation(summary = "Confirm the delivery of the pot",
            description = "Only the organizer, once every member has paid or been covered. Pozzo does not move the "
                    + "money: it records the confirmation and opens the next period, or closes the cycle after the "
                    + "last turn.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pot delivered",
                    content = @Content(schema = @Schema(implementation = PotDeliveryResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The period does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The pot is not complete or was already delivered",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> deliverPot(@AuthenticationPrincipal AuthenticatedMember member,
                                        @PathVariable UUID periodId) {
        var result = cycleCommandService.handle(new DeliverPotCommand(periodId, member.accountId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                delivery -> new PotDeliveryResource(
                        delivery.deliveredPeriod().getId(),
                        delivery.deliveredPeriod().getTurnNumber(),
                        delivery.deliveredPeriod().payoutAmount().amount(),
                        delivery.cycle().getStatus(),
                        delivery.nextPeriod() == null ? null : delivery.nextPeriod().getId(),
                        delivery.nextPeriod() == null ? null : delivery.nextPeriod().getTurnNumber()),
                HttpStatus.OK);
    }

    @PutMapping(value = "/payout/proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Attach the transfer of the pot",
            description = "Keeps a JPEG, PNG or WebP image of up to 2 MB of the transfer with which the organizer "
                    + "delivered the pot, in a private storage, so every member can see it. Only the organizer, once "
                    + "the pot was delivered; attaching another one replaces it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image kept",
                    content = @Content(schema = @Schema(implementation = PayoutProofResource.class))),
            @ApiResponse(responseCode = "400", description = "Not an image, or too large",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The period does not exist or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The pot was not delivered yet",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "503", description = "The receipt storage did not answer or is not configured",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> attachPayoutProof(@AuthenticationPrincipal AuthenticatedMember member,
                                               @PathVariable UUID periodId,
                                               @RequestPart("image") MultipartFile image) throws IOException {
        var result = cycleCommandService.handle(new AttachPayoutProofCommand(periodId, member.accountId(),
                image.getBytes(), image.getContentType()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                period -> new PayoutProofResource(period.getId(), period.hasPayoutProof()), HttpStatus.OK);
    }

    @GetMapping("/payout/proof")
    @Operation(summary = "See the transfer of the pot",
            description = "A signed link to the image the organizer attached, that works for 15 minutes. Any member "
                    + "of the cycle.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Temporary link",
                    content = @Content(schema = @Schema(implementation = ReceiptImageResource.class))),
            @ApiResponse(responseCode = "404", description = "No image, or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "503", description = "The receipt storage did not answer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getPayoutProof(@AuthenticationPrincipal AuthenticatedMember member,
                                            @PathVariable UUID periodId) {
        return contributionQueryService.handle(new GetPayoutProofQuery(periodId, member.accountId()))
                .<ResponseEntity<?>>map(link -> ResponseEntity.ok(new ReceiptImageResource(link.url(), link.expiresAt())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("PayoutProof", periodId.toString())));
    }
}
