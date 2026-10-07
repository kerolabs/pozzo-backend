package pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.Optional;

/**
 * Phone numbers reserved for demonstrations and tests. They always receive the same code
 * and no SMS is sent to them, so the access flow can be shown without depending on the
 * SMS provider. The list is configured per environment and is empty unless set.
 */
public interface TestPhoneNumbers {

    /**
     * Returns the fixed code of a test number, or empty when the number is a regular one.
     */
    Optional<String> fixedCodeFor(PhoneNumber phoneNumber);
}
