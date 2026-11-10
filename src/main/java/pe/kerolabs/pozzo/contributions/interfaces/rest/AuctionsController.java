package pe.kerolabs.pozzo.contributions.interfaces.rest;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.contributions.application.commandservices.AuctionCommandService;
import pe.kerolabs.pozzo.contributions.application.queryservices.ContributionQueryService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseAuctionCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.PlaceBidCommand;
import pe.kerolabs.pozzo.contributions.domain.model.queries.GetAuctionQuery;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.AuctionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.CloseAuctionResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PlaceBidResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.transform.AuctionResourceFromViewAssembler;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ErrorResponseAssembler;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * The auction of the turn of a period: see it, bid and close it.
 */
@RestController
@RequestMapping(value = "/api/v1/periods/{periodId}/auction", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auctions", description = "Auction of the turn of each period")
public class AuctionsController {

    private final AuctionCommandService auctionCommandService;
    private final ContributionQueryService contributionQueryService;

    public AuctionsController(AuctionCommandService auctionCommandService,
                              ContributionQueryService contributionQueryService) {
        this.auctionCommandService = auctionCommandService;
        this.contributionQueryService = contributionQueryService;
    }

    @GetMapping
    @Operation(summary = "See the auction of a turn",
            description = "Every bid with the name of the bidder, from the highest to the lowest, and the result once "
                    + "it closes. Any member of the cycle.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Auction of the period",
                    content = @Content(schema = @Schema(implementation = AuctionResource.class))),
            @ApiResponse(responseCode = "404", description = "The period has no auction or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getAuction(@AuthenticationPrincipal AuthenticatedMember member,
                                        @PathVariable UUID periodId) {
        return contributionQueryService.handle(new GetAuctionQuery(periodId, member.accountId()))
                .<ResponseEntity<?>>map(view -> ResponseEntity.ok(
                        AuctionResourceFromViewAssembler.toResourceFromView(view, member.accountId())))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("Auction", periodId.toString())));
    }

    @PostMapping("/bids")
    @Operation(summary = "Bid in the auction of a turn",
            description = "Offers a discount on the pot to collect in this turn; a new bid replaces the previous one. "
                    + "Only the members who have not collected, until the closing day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bid placed",
                    content = @Content(schema = @Schema(implementation = AuctionResource.class))),
            @ApiResponse(responseCode = "404", description = "The period has no auction or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Below the minimum, not less than the pot, the member "
                    + "already collected or the auction closed",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> placeBid(@AuthenticationPrincipal AuthenticatedMember member,
                                      @PathVariable UUID periodId,
                                      @Valid @RequestBody PlaceBidResource resource) {
        var result = auctionCommandService.handle(new PlaceBidCommand(periodId, member.accountId(), resource.amount()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                view -> AuctionResourceFromViewAssembler.toResourceFromView(view, member.accountId()), HttpStatus.OK);
    }

    @PostMapping("/close")
    @Operation(summary = "Close the auction of a turn",
            description = "Gives the turn to the highest bid (the first one placed wins a tie). Without bids, the turn "
                    + "goes by draw, or to the member the organizer chooses when the group decided so. Only the "
                    + "organizer; the auction also closes by itself the day after its closing day.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Auction closed",
                    content = @Content(schema = @Schema(implementation = AuctionResource.class))),
            @ApiResponse(responseCode = "403", description = "The requester is not the organizer",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "The period has no auction or the requester is not a member",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Already closed, or nobody bid and the organizer must "
                    + "choose a member who has not collected",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> closeAuction(@AuthenticationPrincipal AuthenticatedMember member,
                                          @PathVariable UUID periodId,
                                          @RequestBody(required = false) CloseAuctionResource resource) {
        var result = auctionCommandService.handle(new CloseAuctionCommand(periodId, member.accountId(),
                resource == null ? null : resource.winnerMembershipId()));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                view -> AuctionResourceFromViewAssembler.toResourceFromView(view, member.accountId()), HttpStatus.OK);
    }
}
