package pe.kerolabs.pozzo.contributions.infrastructure.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.application.commandservices.AuctionCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.CloseDueAuctionsCommand;

/**
 * Closes the auctions whose closing day passed, every hour by default.
 */
@Slf4j
@Component
public class AuctionClosingScheduler {

    private final AuctionCommandService auctionCommandService;

    public AuctionClosingScheduler(AuctionCommandService auctionCommandService) {
        this.auctionCommandService = auctionCommandService;
    }

    @Scheduled(fixedDelayString = "${auctions.closing-interval-ms:3600000}",
            initialDelayString = "${auctions.closing-interval-ms:3600000}")
    public void closeDueAuctions() {
        try {
            auctionCommandService.handle(new CloseDueAuctionsCommand());
        } catch (RuntimeException e) {
            log.warn("Closing the due auctions failed", e);
        }
    }
}
