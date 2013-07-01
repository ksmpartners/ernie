package com.ksmpartners.ernie.util;

/**
 * This class wraps an exception that could be thrown during
 * the certificate verification process.
 *
 * Found this on the net:
 * http://www.nakov.com/blog/2009/12/01/x509-certificate-validation-in-java-build-and-verify-chain-and-verify-clr-with-bouncy-castle/
 *
 * @author Svetlin Nakov
 */
public class CertificateVerificationException extends Exception {
    private static final long serialVersionUID = 1L;

    public CertificateVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateVerificationException(String message) {
        super(message);
    }
}
