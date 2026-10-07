package pe.kerolabs.pozzo.iam.application.commandservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.CompleteRegistrationCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.SignOutCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyCodeCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for passwordless access with the phone number.
 */
public interface AuthenticationCommandService {

    /**
     * Sends a new code by SMS and invalidates the pending ones for the same number.
     *
     * @return the issued code, or an error when the previous one was sent less than thirty seconds ago
     */
    Result<VerificationCode, ApplicationError> handle(RequestCodeCommand command);

    /**
     * Verifies a code. Opens a session when the number has an account,
     * or returns a registration token when it does not.
     *
     * @return the verification, or an error when the code is wrong, expired or blocked
     */
    Result<CodeVerification, ApplicationError> handle(VerifyCodeCommand command);

    /**
     * Creates the account of a verified number and opens its first session.
     *
     * @return the signed-in member, or an error when the token is invalid or the terms were not accepted
     */
    Result<AuthenticatedAccount, ApplicationError> handle(CompleteRegistrationCommand command);

    /**
     * Revokes the session of the authenticated member.
     *
     * @return the revoked session, or an error when it does not exist
     */
    Result<Session, ApplicationError> handle(SignOutCommand command);
}
