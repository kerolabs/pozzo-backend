package pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms;

import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * Outbound contract towards the SMS provider. The context only knows this interface;
 * the adapter to the chosen provider lives in the infrastructure layer.
 */
public interface SmsSender {

    /**
     * Sends a text message to a phone number.
     *
     * @param phoneNumber the recipient
     * @param message     the text of the message
     */
    void send(PhoneNumber phoneNumber, String message);
}
