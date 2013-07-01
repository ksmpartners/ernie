package com.ksmpartners.ernie.util;

/**
 * Parsing Exception class
 *
 * @author tmulle
 *
 */
public class SAMLParseException extends Exception {

    public SAMLParseException() {
        super();
    }

    public SAMLParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SAMLParseException(String message) {
        super(message);
    }

    public SAMLParseException(Throwable cause) {
        super(cause);
    }
}