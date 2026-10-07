package pe.kerolabs.pozzo.shared.application.exceptions;

import lombok.Getter;

/**
 * Thrown by an adapter when an external service the operation depends on fails,
 * e.g. the SMS provider. The REST layer maps it to {@code 503 Service Unavailable}, and the
 * transaction of the operation is rolled back.
 */
@Getter
public class ExternalServiceException extends RuntimeException {

    /** Machine-readable code of the failure, e.g. "SMS_DELIVERY_FAILED". */
    private final String code;

    public ExternalServiceException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
