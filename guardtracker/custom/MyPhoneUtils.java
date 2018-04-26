package com.patri.guardtracker.custom;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.Locale;

final class PhoneProcessorData {
    String national;
    int len;
    int cntDigits;
}
/**
 * Created by patri on 06/10/2016.
 */
final public class MyPhoneUtils {
    static final String TAG = MyPhoneUtils.class.getSimpleName();
    static final int MIN_PHONE_DIGITS = 9;
    static final int MAX_PHONE_DIGITS = 15;

    static final String getDefaultCountryCallingCode() {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String countryISO3166_1_alpha_2 = Locale.getDefault().getCountry();
        int countryCode = phoneUtil.getCountryCodeForRegion(countryISO3166_1_alpha_2);
        return "+" + countryCode;
    }
    static PhoneProcessorData processRaw(String phoneRaw) throws NumberParseException {
        int len = phoneRaw.length();

        StringBuilder strBuilder = new StringBuilder();
        while (len > MIN_PHONE_DIGITS && Character.isWhitespace(phoneRaw.charAt(len-1)))
            len = len - 1;
        int cntDigits = 0;
        while (cntDigits < MIN_PHONE_DIGITS && len > 0) {
            char c = phoneRaw.charAt(len - 1);
            if (Character.isDigit(c)) {
                strBuilder.insert(0, c);
                cntDigits = cntDigits + 1;
                len = len - 1;
                continue;
            }
            if (Character.isWhitespace(c)) {
                len = len - 1;
                continue;
            }
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Invalid character in phone number");
        }
        if (cntDigits < MIN_PHONE_DIGITS)
            throw new NumberParseException(NumberParseException.ErrorType.TOO_SHORT_AFTER_IDD, "Invalid phone number format");

        PhoneProcessorData phone = new PhoneProcessorData();
        phone.cntDigits = cntDigits;
        phone.len = len;
        phone.national = strBuilder.toString();
        return phone;
    }
    public final static String formatNational(String phoneRaw) throws NumberParseException {

        PhoneProcessorData data = processRaw(phoneRaw);
        int len = data.len;
        int cntDigits = data.cntDigits;

        while (len > 0 && cntDigits <= MAX_PHONE_DIGITS) {
            char c = phoneRaw.charAt(len - 1);
            if (Character.isDigit(c) == true)
                cntDigits = cntDigits + 1;
            if (c != '+' && Character.isWhitespace(c) == false)
                break;
            len = len - 1;
            if (c == '+') // No more characters (except whitespaces) are allowed
                break;
        }
        while (len > 0) {
            char c = phoneRaw.charAt(len - 1);
            if (Character.isWhitespace(c) == false)
                break;
            len = len - 1;
        }
        if (len > 0)
            throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "Excessive characteres in phone number");

        String s = data.national;
        return s;
    }

    public final static String formatE164(String phoneRaw) throws NumberParseException {

        PhoneProcessorData data = processRaw(phoneRaw);
        int len = data.len;
        int cntDigits = data.cntDigits;

        StringBuilder sb = new StringBuilder();
        while (len > 0 && cntDigits <= MAX_PHONE_DIGITS) {
            char c = phoneRaw.charAt(len - 1);
            if (Character.isDigit(c) == true || c == '+') {
                sb.insert(0, c);
                cntDigits = cntDigits + 1;
                len = len - 1;
            }
            if (Character.isWhitespace(c) == true)
                len = len - 1;
            if (c == '+' || Character.isWhitespace(c) == false && Character.isDigit(c) == false) // No more characters are allowed
                break;
        }
        while (len > 0) {
            char c = phoneRaw.charAt(len - 1);
            if (Character.isWhitespace(c) == false)
                break;
            len = len - 1;
        }
        if (len > 0)
            throw new NumberParseException(NumberParseException.ErrorType.TOO_LONG, "Excessive characters in phone number");

        String defaultCountryCallingCode = getDefaultCountryCallingCode();
        String countryCallingCode = defaultCountryCallingCode;
        if (sb.length() > 0)
            countryCallingCode = sb.toString();
        if (countryCallingCode.equals(defaultCountryCallingCode) == false)
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, "Invalid phone format");

        String s = countryCallingCode + data.national;
        return s;
    }
}
