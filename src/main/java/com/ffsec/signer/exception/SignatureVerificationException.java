package com.ffsec.signer.exception;

/**
 * Exception thrown when error occured during signature generation's or verification's process.
 *
 * @author Federico Farinetto
 */
public class SignatureVerificationException extends Exception {

    public SignatureVerificationException() {
    }

    public SignatureVerificationException(String message) {
        super(message);
    }

}
