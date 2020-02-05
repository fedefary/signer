package com.ffsec.signer.aspect;

import com.ffsec.signer.config.SignatureConfigManager;
import com.ffsec.signer.exception.FingerprintVerificationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

    Mac mac;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @Autowired
    private HttpServletRequest request;

    @PostConstruct
    void initMac() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Before("@annotation(com.ffsec.signer.annotations.Signed)")
    public void preHandle() throws FingerprintVerificationException, IOException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String body = request.getReader().lines().collect(Collectors.joining());

        if(body != null) {

            String signatureHeader = this.request.getHeader("Signature");

            if (signatureHeader == null) {
                throw new FingerprintVerificationException();
            }

            byte[] receivedSignature;

            try {
                receivedSignature = Base64.getDecoder().decode(signatureHeader);
            } catch (IllegalArgumentException ex) {
                throw new FingerprintVerificationException();
            }

            byte[] byteKey = signatureConfigManager.getMyKey();
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, signatureConfigManager.getAlgorithm());
            try {
                mac.init(keySpec);
            } catch (InvalidKeyException e) {
                throw new FingerprintVerificationException();
            }
            byte[] calculatedSignature = mac.doFinal(body.getBytes());

            boolean flag = Arrays.equals(receivedSignature, calculatedSignature);

            if (!flag) {
                throw new FingerprintVerificationException();
            }

        }

    }

}
