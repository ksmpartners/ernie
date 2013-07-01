package com.ksmpartners.ernie.util;


/**
 * Validation Exception class
 *
 * @author tmulle
 */
public class SAMLValidationException extends Exception {

    public SAMLValidationException() {
    }

    public SAMLValidationException(Throwable cause) {
        super(cause);
    }

    public SAMLValidationException(String message) {
        super(message);
    }

    public SAMLValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
