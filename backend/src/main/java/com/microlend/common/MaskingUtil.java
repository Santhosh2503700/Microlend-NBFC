package com.microlend.common;


public final class MaskingUtil {

    private MaskingUtil() {
    }

    /** 123456789012 -> XXXX-XXXX-9012 */
    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) {
            return "XXXX-XXXX-XXXX";
        }
        String last4 = aadhaar.substring(aadhaar.length() - 4);
        return "XXXX-XXXX-" + last4;
    }

    /** Shows only the last 4 digits of a bank account. */
    public static String maskAccount(String account) {
        if (account == null || account.length() < 4) {
            return "XXXX";
        }
        String last4 = account.substring(account.length() - 4);
        return "XXXXXX" + last4;
    }
}
