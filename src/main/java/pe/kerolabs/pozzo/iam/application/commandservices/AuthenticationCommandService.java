package pe.kerolabs.pozzo.iam.application.commandservices;

import pe.kerolabs.pozzo.iam.domain.model.aggregates.Session;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.commands.CompleteRegistrationCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RecoverAccountCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.RequestRecoveryPhoneCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.SignOutCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyCodeCommand;
import pe.kerolabs.pozzo.iam.domain.model.commands.VerifyRecoveryCodeCommand;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

/**
 * Application service contract for passwordless access with the phone number, and for recovering an
 * account with the backup email.
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

    /**
     * Step 1 of the recovery: sends a code to the backup email when an active account has it.
     *
     * @return the same answer whether the email has an account or not, or an error when a code was
     * requested less than thirty seconds ago
     */
    Result<RecoveryCodeRequest, ApplicationError> handle(RequestRecoveryCodeCommand command);

    /**
     * Step 2 of the recovery: verifies the email code and returns a recovery token.
     *
     * @return the recovery token, or an error when the code is wrong, expired or blocked
     */
    Result<RecoveryVerification, ApplicationError> handle(VerifyRecoveryCodeCommand command);

    /**
     * Step 3 of the recovery: sends an SMS code to the new number.
     *
     * @return the issued code, or an error when the token is invalid or the number has another account
     */
    Result<VerificationCode, ApplicationError> handle(RequestRecoveryPhoneCodeCommand command);

    /**
     * Step 4 of the recovery: links the verified new number to the account, revokes the sessions of the
     * lost phone and opens a session.
     *
     * @return the signed-in member, or an error when the token or the SMS code is not valid
     */
    Result<AuthenticatedAccount, ApplicationError> handle(RecoverAccountCommand command);
}
