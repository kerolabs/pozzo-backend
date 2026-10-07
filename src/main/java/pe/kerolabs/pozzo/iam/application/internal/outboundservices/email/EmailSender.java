package pe.kerolabs.pozzo.iam.application.internal.outboundservices.email;

/**
 * Outbound contract towards the email provider. The context only knows this interface;
 * the adapter to the chosen provider lives in the infrastructure layer.
 */
public interface EmailSender {

    /**
     * Sends a plain-text email.
     *
     * @param to      the recipient
     * @param subject the subject
     * @param text    the body
     */
    void send(String to, String subject, String text);
}
