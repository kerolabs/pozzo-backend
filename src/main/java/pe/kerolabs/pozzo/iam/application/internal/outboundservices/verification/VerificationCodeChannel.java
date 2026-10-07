package pe.kerolabs.pozzo.iam.application.internal.outboundservices.verification;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Channel that delivers a verification code to a phone number and later checks the code the
 * member typed. Either Pozzo generates the code and sends it by SMS, or an external verification
 * service generates, sends and checks it. The rules of the aggregate (validity, attempts and the
 * waiting time to request a new code) apply the same way in both cases.
 */
public interface VerificationCodeChannel {

    /**
     * Delivers a new code to the phone number.
     *
     * @return the value the aggregate stores: the hash of the code, or a marker when the
     *         external service keeps the code
     */
    String deliver(PhoneNumber phoneNumber);

    /**
     * Checks the code typed by the member.
     *
     * @param phoneNumber the number the code was delivered to
     * @param input       the code typed by the member
     * @param storedValue the value returned by {@link #deliver(PhoneNumber)}
     * @return true when the code is correct
     */
    boolean check(PhoneNumber phoneNumber, String input, String storedValue);
}
