package no.jhommeland.paymentapi.util;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class UrlUtil {

    private static final String URL_PARAM_START = "?";

    private static final String URL_PARAM_SEPARATOR = "&";

    public static String toBase64(String resultJson) {
        return Base64.getEncoder().encodeToString(resultJson.getBytes());
    }

    public static String addUrlParameter(String baseUrl, String name, String value) {
        String separator = baseUrl.contains(URL_PARAM_START) ? URL_PARAM_SEPARATOR : URL_PARAM_START;
        return baseUrl + separator + name + "=" + value;
    }

}
