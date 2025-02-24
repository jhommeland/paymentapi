package no.jhommeland.paymentapi.util;

import no.jhommeland.paymentapi.model.ErrorResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

/**
 *
 * Utility class to help with creating responses.
 *
 * @author Joakim Hommeland
 */
public class ResponseUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResponseUtil.class);

    public static ResponseEntity<ErrorResponseModel> createResponse(HttpHeaders headers, HttpStatusCode status, String message) {
        ErrorResponseModel response = new ErrorResponseModel(status.toString(), message);
        return new ResponseEntity<>(response, headers, status);
    }

}
