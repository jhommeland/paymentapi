package no.jhommeland.paymentapi.util;

import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class UrlUtil {

    private static final String URL_PARAM_START = "?";

    private static final String URL_PARAM_SEPARATOR = "&";

    private static final String URL_PARAM_VALUE_ASSIGN = "=";

    public static String toBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String addUrlParameter(String baseUrl, String name, String value) {
        String separator = baseUrl.contains(URL_PARAM_START) ? URL_PARAM_SEPARATOR : URL_PARAM_START;
        return baseUrl + separator + name + "=" + value;
    }

    public static Map<String,String> parseUrlParameters(String query, boolean decode) {
        if (decode) {
            query = URLDecoder.decode(query, StandardCharsets.UTF_8);
        }

        String[] params = query.split(URL_PARAM_SEPARATOR);
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String name = param.split(URL_PARAM_VALUE_ASSIGN)[0];
            String value = param.split(URL_PARAM_VALUE_ASSIGN)[1];
            map.put(name, value);
        }

        return map;

    }

}
