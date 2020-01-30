package com.ffsecurity.signer.aspect;

import com.ffsecurity.signer.config.SigningConfigManager;
import com.ffsecurity.signer.exception.FingerprintVerificationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    SigningConfigManager signingConfigManager;

    @Autowired
    private HttpServletRequest request;

    @PostConstruct
    void initMessageDigest() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(signingConfigManager.getAlgorithm());
    }

    @Before("@annotation(com.ffsecurity.signer.annotations.Signed)")
    public void preHandle() throws FingerprintVerificationException {

        String fingerPrintHeader = request.getHeader("Fingerprint");
        String seedHeader = request.getHeader("Seed");

        if(fingerPrintHeader == null || seedHeader == null) {
            throw new FingerprintVerificationException();
        }

        byte[] fingerPrint = Base64.getDecoder().decode(fingerPrintHeader);
        byte[] seed = Base64.getDecoder().decode(seedHeader);
        byte[] body = new byte[0];
        try {
            body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())).getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Calculating salt */

        byte[] salt = signingConfigManager.calculateXor(seed,signingConfigManager.getMyKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(body);
            outputStream.write(salt);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Calculating fingerprint header */

        byte[] hash = null;
        messageDigest.reset();
        messageDigest.update(outputStream.toByteArray());
        hash = messageDigest.digest();

        /* Fingerprint verification */

        boolean flag = Arrays.equals(hash,fingerPrint);

        if(!flag) {
            throw new FingerprintVerificationException();
        }

    }

}
