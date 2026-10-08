package pe.kerolabs.pozzo.iam.domain.model.queries;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Query for the account registered with a phone number, if any.
 *
 * @param phoneNumber the phone number
 */
public record GetAccountByPhoneQuery(PhoneNumber phoneNumber) {
}
