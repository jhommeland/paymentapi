package no.jhommeland.paymentapi.util;

public class TerminalUtil {

    public static String buildTransactionId(String poiId, String serviceId) {
        return poiId + "-" + serviceId;
    }
}
