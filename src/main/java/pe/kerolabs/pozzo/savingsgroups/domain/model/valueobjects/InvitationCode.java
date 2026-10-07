package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Short code a member types to join a savings group, e.g. "JB-7K4M": the initials of the group
 * and four random characters. The alphabet leaves out characters that are easy to confuse
 * (0 and O, 1 and I).
 *
 * @param value the code, in upper case
 */
public record InvitationCode(String value) {

    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RANDOM_LENGTH = 4;
    private static final Pattern FORMAT = Pattern.compile("[A-Z]{2}-[" + ALPHABET + "]{" + RANDOM_LENGTH + "}");
    private static final SecureRandom RANDOM = new SecureRandom();

    public InvitationCode {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("The invitation code must look like JB-7K4M");
        }
    }

    /**
     * Generates a code for a group, using the initials of its name as prefix.
     */
    public static InvitationCode random(String groupName) {
        var suffix = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            suffix.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new InvitationCode(prefixOf(groupName) + "-" + suffix);
    }

    /**
     * Parses a code typed by a member: case and spaces do not matter, and the dash is optional.
     */
    public static InvitationCode parse(String typed) {
        var compact = typed == null ? "" : typed.replaceAll("[\\s-]", "").toUpperCase();
        if (compact.length() != 2 + RANDOM_LENGTH) {
            throw new IllegalArgumentException("The invitation code must have six characters");
        }
        return new InvitationCode(compact.substring(0, 2) + "-" + compact.substring(2));
    }

    private static String prefixOf(String groupName) {
        var letters = Normalizer.normalize(groupName == null ? "" : groupName, Normalizer.Form.NFD)
                .replaceAll("[^A-Za-z ]", "")
                .trim()
                .toUpperCase();
        var words = letters.isEmpty() ? new String[0] : letters.split("\\s+");
        var prefix = new StringBuilder();
        for (var word : words) {
            if (prefix.length() < 2 && !word.isEmpty() && !isConnector(word)) {
                prefix.append(word.charAt(0));
            }
        }
        if (prefix.length() < 2 && words.length > 0 && words[0].length() >= 2) {
            prefix.setLength(0);
            prefix.append(words[0], 0, 2);
        }
        while (prefix.length() < 2) {
            prefix.append("PZ".charAt(prefix.length()));
        }
        return prefix.toString();
    }

    private static boolean isConnector(String word) {
        return switch (word) {
            case "DE", "DEL", "LA", "LAS", "LOS", "EL", "Y" -> true;
            default -> false;
        };
    }
}
