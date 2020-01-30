package com.ffsecurity.signer.exceptionhandler;

import com.ffsecurity.signer.exception.FingerprintVerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class FingerprintExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String FINGERPRINT_VERIFICATION_FAILED = "";

    @ExceptionHandler(FingerprintVerificationException.class)
    public final ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(FINGERPRINT_VERIFICATION_FAILED);
    }
}
