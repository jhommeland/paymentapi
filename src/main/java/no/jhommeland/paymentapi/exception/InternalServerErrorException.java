package no.jhommeland.paymentapi.exception;

/**
 *
 * Internal Server Error Exception
 *
 * @author Joakim Hommeland
 */
public class InternalServerErrorException extends RuntimeException{

    public InternalServerErrorException() {
    }

    public InternalServerErrorException(String message) {
        super(message);
    }
}
