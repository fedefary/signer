package com.ffsec.signer.aspect;

import com.ffsec.signer.config.SignatureConfigManager;
import com.ffsec.signer.exception.SignatureVerificationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;


@Component
@Aspect
public class SignedAspect {

    Logger logger = LoggerFactory.getLogger(SignedAspect.class);

    @Autowired
    SignatureConfigManager signatureConfigManager;


    @Before("@annotation(com.ffsec.signer.annotations.Signed)")
    public void preHandle() throws SignatureVerificationException, IOException {

        boolean isTraceEnabled = logger.isTraceEnabled();

        ContentCachingRequestWrapper request = (ContentCachingRequestWrapper) ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        byte[] body = request.getContentAsByteArray();

        if(body.length == 0) {
            body = StreamUtils.copyToByteArray(request.getInputStream());
        }

        String signatureHeader = request.getHeader("Signature");

        byte[] receivedSignature;

        if (signatureHeader != null) {

            try {
                receivedSignature = Base64.getDecoder().decode(signatureHeader);
            } catch (IllegalArgumentException ex) {
                throw new SignatureVerificationException("The supplied signature is not valid");
            }

            if (isTraceEnabled) {
                logger.trace("The received signature is {}", receivedSignature);
            }

            byte[] calculatedSignature;

            if (body.length > 0) {

                if (isTraceEnabled) {
                    logger.trace("The request contains a body, signature's verification started");
                }

                calculatedSignature = signatureConfigManager.generateSignature(body);


            } else {

                if (isTraceEnabled) {
                    logger.trace("The request does not contain a body, using custom header");
                    logger.trace("Signature's verification started");
                }

                String seedHeader = request.getHeader("Seed");
                byte[] seed;

                try {
                    seed = Base64.getDecoder().decode(seedHeader);
                } catch (IllegalArgumentException ex) {
                    throw new SignatureVerificationException("The randomic generated seed can't be decoded");
                }

                calculatedSignature = signatureConfigManager.generateSignature(seed);

            }

            boolean flag = Arrays.equals(receivedSignature, calculatedSignature);

            if (!flag) {
                if (isTraceEnabled) {
                    logger.trace("Verification's process finished, the signature is not valid");
                }
                throw new SignatureVerificationException("The supplied signature is not valid");
            } else if (isTraceEnabled) {
                logger.trace("Verification's process finished, the signature is valid");
            }

        } else {
            if (isTraceEnabled) {
                logger.trace("The Signature header is not present");
            }
            throw new SignatureVerificationException("The Signature header is not present");
        }

    }

}
