package com.ffsec.signer.exceptionhandler;

import com.ffsec.signer.exception.FingerprintVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class FingerprintExceptionHandler extends ResponseEntityExceptionHandler {

    Logger logger = LoggerFactory.getLogger(FingerprintExceptionHandler.class);

    private static final String FINGERPRINT_VERIFICATION_ERROR = "The supplied signature is not valid";

    @ExceptionHandler(FingerprintVerificationException.class)
    public final ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        logger.warn(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FINGERPRINT_VERIFICATION_ERROR);
    }

}
