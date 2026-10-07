package pe.kerolabs.pozzo.iam.domain.model.commands;

import org.jspecify.annotations.Nullable;

/**
 * Command to create the account of a member whose phone number was just verified.
 *
 * @param registrationToken the token returned by the verification, which proves the phone number
 * @param displayName       the name shown to the group
 * @param photoUrl          an optional photo
 * @param termsAccepted     whether the member accepted the Terms and Conditions and the Privacy Policy
 * @param deviceLabel       an optional name of the device where the session opens
 */
public record CompleteRegistrationCommand(String registrationToken, String displayName, @Nullable String photoUrl,
                                          boolean termsAccepted, @Nullable String deviceLabel) {
}
