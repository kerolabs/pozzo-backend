package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Mobile phone number that identifies a member. Pozzo only accepts Peruvian mobiles:
 * country code 51 and nine digits starting with 9.
 *
 * @param countryCode the country calling code, without the plus sign
 * @param number      the national number
 */
public record PhoneNumber(String countryCode, String number) {

    public static final String PERU_COUNTRY_CODE = "51";
    private static final Pattern PERUVIAN_MOBILE = Pattern.compile("9\\d{8}");

    public PhoneNumber {
        if (!PERU_COUNTRY_CODE.equals(countryCode)) {
            throw new IllegalArgumentException("Only Peruvian phone numbers (+51) are supported");
        }
        if (number == null || !PERUVIAN_MOBILE.matcher(number).matches()) {
            throw new IllegalArgumentException("The phone number must have nine digits and start with 9");
        }
    }

    /**
     * Creates a Peruvian mobile number from its nine national digits; spaces are ignored.
     *
     * @param number the national number, e.g. "999 000 123"
     * @return the phone number
     */
    public static PhoneNumber ofPeruvianMobile(String number) {
        return new PhoneNumber(PERU_COUNTRY_CODE, number == null ? null : number.replace(" ", ""));
    }

    /**
     * Parses a number in E.164 format, e.g. "+51999000123".
     *
     * @param e164 the number in E.164 format
     * @return the phone number
     */
    public static PhoneNumber fromE164(String e164) {
        var prefix = "+" + PERU_COUNTRY_CODE;
        if (e164 == null || !e164.startsWith(prefix)) {
            throw new IllegalArgumentException("Only Peruvian phone numbers (+51) are supported");
        }
        return new PhoneNumber(PERU_COUNTRY_CODE, e164.substring(prefix.length()));
    }

    /**
     * Returns the number in E.164 format, the form in which it is stored and sent to SMS providers.
     */
    public String e164() {
        return "+" + countryCode + number;
    }

    /**
     * Returns true when the number is a Peruvian mobile, which the constructor already guarantees.
     */
    public boolean isPeruvianMobile() {
        return PERU_COUNTRY_CODE.equals(countryCode) && PERUVIAN_MOBILE.matcher(number).matches();
    }
}
