package pe.kerolabs.pozzo.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.interfaces.rest.resources.ErrorResource;

import java.util.Locale;

/**
 * Converts application errors into HTTP responses with a localized message.
 */
@NullMarked
public final class ErrorResponseAssembler {

    private ErrorResponseAssembler() {
    }

    /**
     * Maps an ApplicationError to a ResponseEntity whose status follows the error code.
     *
     * @param error the application error
     * @return the response entity with the error body
     */
    public static ResponseEntity<ErrorResource> toErrorResponseFromApplicationError(ApplicationError error) {
        var resource = new ErrorResource(error.code(), toLocalizedMessage(error), error.details());
        return new ResponseEntity<>(resource, toStatusFromErrorCode(error.code()));
    }

    /**
     * Determines the HTTP status for an error code.
     *
     * @param errorCode the error code (e.g. "SAVINGS_GROUP_NOT_FOUND", "VALIDATION_ERROR")
     * @return the matching HTTP status
     */
    public static HttpStatusCode toStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatus.UNPROCESSABLE_CONTENT;
            case String s when s.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND;
            case String s when s.endsWith("_CONFLICT") -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static String toLocalizedMessage(ApplicationError error) {
        var entityName = toEntityNameFromErrorCode(error.code());
        var specific = LocalizedMessages.resolveOrNull(toSpecificMessageKey(error.code()), error.details(), entityName);
        if (specific != null) {
            return specific;
        }
        return LocalizedMessages.resolveOrDefault(toGenericMessageKey(error.code()), error.message(), error.details(), entityName);
    }

    private static String toSpecificMessageKey(String errorCode) {
        return "error.%s.message".formatted(errorCode.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

    private static String toGenericMessageKey(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> "error.validation.message";
            case "UNAUTHORIZED" -> "error.unauthorized.message";
            case "FORBIDDEN" -> "error.forbidden.message";
            case "BUSINESS_RULE_VIOLATION" -> "error.business-rule.message";
            case "UNEXPECTED_ERROR" -> "error.unexpected.message";
            case String s when s.endsWith("_NOT_FOUND") -> "error.not-found.message";
            case String s when s.endsWith("_CONFLICT") -> "error.conflict.message";
            default -> "error.generic.message";
        };
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
