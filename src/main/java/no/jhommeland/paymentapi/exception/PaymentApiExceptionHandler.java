package no.jhommeland.paymentapi.exception;

import no.jhommeland.paymentapi.model.ErrorResponseModel;
import no.jhommeland.paymentapi.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * Exception Handler that converts the exception into the proper error response.
 *
 * @author Joakim Hommeland
 */
@ControllerAdvice
public class PaymentApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(PaymentApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseModel> handleResponseStatusException(ResponseStatusException e, WebRequest request) {
        logger.info("Invalid Request: Message={}", e.getReason());
        return ResponseUtil.createResponse(e.getHeaders(), e.getStatusCode(), e.getReason());
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ErrorResponseModel> handleInternalServerErrorException(InternalServerErrorException e, WebRequest request) {
        logger.error("Internal Server Exception Occurred: Message={}", e.getMessage());
        return ResponseUtil.createResponse(new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

}
