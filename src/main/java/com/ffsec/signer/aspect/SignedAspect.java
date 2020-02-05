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
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


@Component
@Aspect
public class SignedAspect {

    MessageDigest messageDigest;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @Autowired
    private HttpServletRequest request;

    @PostConstruct
    void initMessageDigest() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Before("@annotation(com.ffsec.signer.annotations.Signed)")
    public void preHandle() throws FingerprintVerificationException, IOException {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String body = request.getReader().lines().collect(Collectors.joining());

        if(body != null) {

            String signatureHeader = this.request.getHeader("Signature");
            String seedHeader = this.request.getHeader("Seed");

            if (signatureHeader == null || seedHeader == null) {
                throw new FingerprintVerificationException();
            }

            byte[] signature;
            byte[] seed;

            try {
                signature = Base64.getDecoder().decode(signatureHeader);
                seed = Base64.getDecoder().decode(seedHeader);
            } catch (IllegalArgumentException ex) {
                throw new FingerprintVerificationException();
            }

            /* Calculating xored key */

            byte[] xoredKey = signatureConfigManager.calculateXor(seed, signatureConfigManager.getMyKey());

            /* Concatenating xor result with body byte array */

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(xoredKey);
            outputStream.write(body.getBytes());

            /* Calculating fingerprint header */

            messageDigest.reset();
            messageDigest.update(outputStream.toByteArray());
            byte[] hash = messageDigest.digest();

            /* Fingerprint verification */

            boolean flag = Arrays.equals(hash, signature);

            if (!flag) {
                throw new FingerprintVerificationException();
            }

        }

    }

}
