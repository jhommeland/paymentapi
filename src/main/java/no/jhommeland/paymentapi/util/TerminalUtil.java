package no.jhommeland.paymentapi.util;

public class TerminalUtil {

    private static String TERMINAL_DEFAULT_LANGUAGE = "en";

    public static String buildTransactionId(String poiId, String serviceId) {
        return poiId + "-" + serviceId;
    }

    public static String localeToIsoLanguage(String locale) {
        try {
            return locale.split("-")[0];
        } catch (Exception e) {
            return TERMINAL_DEFAULT_LANGUAGE;
        }
    }
}
