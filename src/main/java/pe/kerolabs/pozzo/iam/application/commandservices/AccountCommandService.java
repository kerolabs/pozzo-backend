package pe.kerolabs.pozzo.iam.application.commandservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Account;
import pe.kerolabs.pozzo.iam.domain.model.commands.UpdateProfileCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for changes to an account.
 */
public interface AccountCommandService {

    /**
     * Changes the display name, photo and theme of a member.
     *
     * @return the updated account, or an error when it does not exist
     */
    Result<Account, ApplicationError> handle(UpdateProfileCommand command);
}
