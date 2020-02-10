package com.ffsec.signer.exceptionhandler;

import com.ffsec.signer.exception.SignatureVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * {@link ExceptionHandler} for {@link SignatureVerificationException}
 *
 * @author Federico Farinetto
 */
@ControllerAdvice
public class SignatureVerificationExceptionHandler extends ResponseEntityExceptionHandler {

    Logger logger = LoggerFactory.getLogger(SignatureVerificationExceptionHandler.class);

    @ExceptionHandler(SignatureVerificationException.class)
    public final ResponseEntity<String> handleAllExceptions(SignatureVerificationException ex, WebRequest request) {

        if(logger.isErrorEnabled()) {
            logger.error(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());

    }

}
