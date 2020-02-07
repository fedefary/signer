package com.ffsec.signer.aspect;

import com.ffsec.signer.config.SignatureConfigManager;
import com.ffsec.signer.exception.SignatureVerificationException;
import com.ffsec.signer.utils.SignerUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


@Component
@Aspect
public class SignedAspect {

    Logger logger = LoggerFactory.getLogger(SignedAspect.class);

    @Autowired
    SignatureConfigManager signatureConfigManager;


    @Before("@annotation(com.ffsec.signer.annotations.Signed)")
    public void preHandle() throws SignatureVerificationException, IOException {

        boolean isTraceEnabled = logger.isTraceEnabled();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String body = request.getReader().lines().collect(Collectors.joining());

        String signatureHeader = request.getHeader("Signature");

        byte[] receivedSignature;

        if (signatureHeader != null) {

            try {
                receivedSignature = Base64.getDecoder().decode(signatureHeader);
            } catch (IllegalArgumentException ex) {
                throw new SignatureVerificationException(ex.getMessage());
            }

            if (isTraceEnabled) {
                logger.debug("The received signature is {}", receivedSignature);
            }

            byte[] calculatedSignature = null;

            if (body != null) {

                if (isTraceEnabled) {
                    logger.debug("The request contains a body, signature's verification started");
                }

                calculatedSignature = signatureConfigManager.generateSignature(body.getBytes());


            } else if (!request.getParameterMap().isEmpty()) {

                if (isTraceEnabled) {
                    logger.debug("The request does not contain a body but only parameters, signature's verification started");
                }

                byte[] paramsByteArray = SignerUtils.convertRequestParameters(request.getParameterMap());

                calculatedSignature = signatureConfigManager.generateSignature(paramsByteArray);

            }

            boolean flag = Arrays.equals(receivedSignature, calculatedSignature);

            if (!flag) {
                if (isTraceEnabled) {
                    logger.debug("Verification's process finished, the signature is not valid");
                }
                throw new SignatureVerificationException();
            } else if (isTraceEnabled) {
                logger.debug("Verification's process finished, the signature is valid");
            }

        } else if (isTraceEnabled) {
            logger.debug("The Signature header is not present, do nothing");
        }

    }

}
