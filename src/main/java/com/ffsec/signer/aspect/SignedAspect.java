package com.ffsec.signer.aspect;

import com.ffsec.signer.config.SignatureConfigManager;
import com.ffsec.signer.exception.FingerprintVerificationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


@Component
@Aspect
public class SignedAspect {

    Logger logger = LoggerFactory.getLogger(SignedAspect.class);

    private Mac mac;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @PostConstruct
    void initMac() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Before("@annotation(com.ffsec.signer.annotations.Signed)")
    public void preHandle() throws FingerprintVerificationException, IOException {

        boolean isDebugEnabled = logger.isDebugEnabled();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String body = request.getReader().lines().collect(Collectors.joining());

        if(body != null) {

            String signatureHeader = request.getHeader("Signature");

            if (signatureHeader == null) {
                if(isDebugEnabled) {
                    logger.debug("Signature header is missing");
                }
                throw new FingerprintVerificationException("Signature header is missing");
            }

            byte[] receivedSignature;

            try {
                receivedSignature = Base64.getDecoder().decode(signatureHeader);
            } catch (IllegalArgumentException ex) {
                throw new FingerprintVerificationException(ex.getMessage());
            }

            if(isDebugEnabled) {
                logger.debug("The received signature is {}", receivedSignature);
                logger.debug("Verification's process started");
            }

            byte[] byteKey = signatureConfigManager.getMyKey();
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, signatureConfigManager.getAlgorithm());
            try {
                mac.init(keySpec);
            } catch (InvalidKeyException e) {
                throw new FingerprintVerificationException(e.getMessage());
            }
            byte[] calculatedSignature = mac.doFinal(body.getBytes());

            boolean flag = Arrays.equals(receivedSignature, calculatedSignature);

            if (!flag) {
                if(isDebugEnabled) {
                    logger.debug("Verification's process finished, the signature is not valid");
                }
                throw new FingerprintVerificationException();
            } else if(isDebugEnabled) {
                logger.debug("Verification's process finished, the signature is valid");
            }

        }

    }

}
