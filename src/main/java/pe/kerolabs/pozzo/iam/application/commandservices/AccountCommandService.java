package pe.kerolabs.pozzo.iam.application.commandservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.ChangePhoneNumberCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestPhoneChangeCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.UpdateProfileCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for changes to an account.
 */
public interface AccountCommandService {

    /**
     * Changes the profile of a member.
     *
     * @return the updated account, or an error when it does not exist or the backup email belongs to
     * another account
     */
    Result<Account, ApplicationError> handle(UpdateProfileCommand command);

    /**
     * Sends an SMS code to the number the member wants to switch to.
     *
     * @return the issued code, or an error when the number is the current one or has another account
     */
    Result<VerificationCode, ApplicationError> handle(RequestPhoneChangeCodeCommand command);

    /**
     * Links the account to the new number once its SMS code is verified. Groups and history stay.
     *
     * @return the updated account, or an error when the code is not valid
     */
    Result<Account, ApplicationError> handle(ChangePhoneNumberCommand command);
}
