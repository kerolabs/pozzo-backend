package pe.kerolabs.pozzo.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.ErrorType;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;

import java.util.Locale;

/**
 * Converts application errors into HTTP responses with a localized message.
 *
 * <p>The status comes from the error type. The message is looked up first by the specific
 * code ({@code error.<code>.message}), then by the type ({@code error.<type>.message}), and
 * falls back to the message carried by the error.</p>
 */
@NullMarked
public final class ErrorResponseAssembler {

    private ErrorResponseAssembler() {
    }

    /**
     * Maps an ApplicationError to a ResponseEntity whose status follows the error type.
     *
     * @param error the application error
     * @return the response entity with the error body
     */
    public static ResponseEntity<ErrorResource> toErrorResponseFromApplicationError(ApplicationError error) {
        var resource = new ErrorResource(error.code(), toLocalizedMessage(error), error.details());
        return new ResponseEntity<>(resource, toStatusFromErrorType(error.type()));
    }

    /**
     * Determines the HTTP status for an error type.
     *
     * @param type the error type
     * @return the matching HTTP status
     */
    public static HttpStatusCode toStatusFromErrorType(ErrorType type) {
        return switch (type) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case BUSINESS_RULE -> HttpStatus.UNPROCESSABLE_CONTENT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static String toLocalizedMessage(ApplicationError error) {
        var entityName = toEntityNameFromErrorCode(error.code());
        var specific = LocalizedMessages.resolveOrNull(toMessageKey(error.code()), error.details(), entityName);
        if (specific != null) {
            return specific;
        }
        return LocalizedMessages.resolveOrDefault(
                toMessageKey(error.type().name()), error.message(), error.details(), entityName);
    }

    private static String toMessageKey(String codeOrType) {
        return "error.%s.message".formatted(codeOrType.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

    private static String toEntityNameFromErrorCode(String errorCode) {
        if (errorCode.endsWith("_NOT_FOUND")) {
            return errorCode.replace("_NOT_FOUND", "").toLowerCase(Locale.ROOT);
        }
        if (errorCode.endsWith("_CONFLICT")) {
            return errorCode.replace("_CONFLICT", "").toLowerCase(Locale.ROOT);
        }
        return "resource";
    }
}
