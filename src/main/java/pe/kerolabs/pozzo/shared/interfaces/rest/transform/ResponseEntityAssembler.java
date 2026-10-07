package pe.kerolabs.pozzo.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;
import pe.kerolabs.pozzo.shared.application.result.Result;

import java.util.function.Function;

/**
 * Translates application {@link Result} values into HTTP responses.
 */
@NullMarked
public final class ResponseEntityAssembler {

    private ResponseEntityAssembler() {
    }

    /**
     * Converts a Result into an HTTP response: a success is mapped with the given resource
     * assembler and status, a failure is delegated to {@link ErrorResponseAssembler}.
     *
     * @param result                   the application result
     * @param successResourceAssembler maps the success value to the response body
     * @param successStatus            the HTTP status for a success
     * @param <T>                      the success value type
     * @param <R>                      the response body type
     * @return the response entity for the success or the failure
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> successResourceAssembler,
            HttpStatusCode successStatus) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
