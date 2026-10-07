package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.PaymentMethod;

import java.util.UUID;

/**
 * Command to define where the contributions of a group are sent.
 */
public record DefineDestinationCommand(UUID groupId, UUID requesterId, PaymentMethod method, String phoneNumber) {
}
