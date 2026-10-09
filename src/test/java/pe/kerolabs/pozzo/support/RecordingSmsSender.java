package pe.kerolabs.pozzo.support;

import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SMS sender for the tests: keeps the last message sent to each number instead of sending it, so a
 * test reads the verification code the same way a member reads it on the phone.
 */
public class RecordingSmsSender implements SmsSender {

    private static final Pattern CODE = Pattern.compile("\\b(\\d{6})\\b");

    private final Map<String, String> lastMessages = new ConcurrentHashMap<>();

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        lastMessages.put(phoneNumber.e164(), message);
    }

    /**
     * Returns the six-digit code of the last SMS sent to a nine-digit Peruvian number.
     */
    public String lastCodeFor(String nationalNumber) {
        var message = lastMessages.get("+51" + nationalNumber);
        if (message == null) {
            throw new IllegalStateException("No SMS was sent to " + nationalNumber);
        }
        var matcher = CODE.matcher(message);
        if (!matcher.find()) {
            throw new IllegalStateException("The SMS has no six-digit code: " + message);
        }
        return matcher.group(1);
    }
}
