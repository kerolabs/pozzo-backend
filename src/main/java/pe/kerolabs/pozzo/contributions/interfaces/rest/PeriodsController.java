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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.DeliverPotCommand;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PotDeliveryResource;
import pe.kerolabs.pozzo.iam.interfaces.acl.AuthenticatedMember;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;
import pe.kerolabs.pozzo.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.util.UUID;

/**
 * Delivery of the pot of a period, which moves the cycle to its next turn or closes it.
 */
@RestController
@RequestMapping(value = "/api/v1/periods/{periodId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Periods", description = "Delivery of the pot")
public class PeriodsController {

    private final CycleCommandService cycleCommandService;

    public PeriodsController(CycleCommandService cycleCommandService) {
        this.cycleCommandService = cycleCommandService;
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
                        delivery.deliveredPeriod().potAmount().amount(),
                        delivery.cycle().getStatus(),
                        delivery.nextPeriod() == null ? null : delivery.nextPeriod().getId(),
                        delivery.nextPeriod() == null ? null : delivery.nextPeriod().getTurnNumber()),
                HttpStatus.OK);
    }
}
