package no.jhommeland.paymentapi.util;

import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class HtmlUtil {

    public String toBase64(String resultJson) {
        return Base64.getEncoder().encodeToString(resultJson.getBytes());
    }

}
