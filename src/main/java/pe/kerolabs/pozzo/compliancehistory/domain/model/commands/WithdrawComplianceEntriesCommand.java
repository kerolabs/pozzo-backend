package pe.kerolabs.pozzo.compliancehistory.domain.model.commands;

import java.util.List;
import java.util.UUID;

/**
 * Takes out of a member's history the facts recorded from these sources, because what caused them was undone.
 */
public record WithdrawComplianceEntriesCommand(UUID accountId, List<String> sourceEventIds) {
}
