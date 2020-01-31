package com.ffsecurity.signer.aspect;

import com.ffsecurity.signer.config.SigningConfigManager;
import com.ffsecurity.signer.exception.FingerprintVerificationException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;


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

        /* Calculating salt */

        byte[] unhashedSign = signingConfigManager.calculateXor(seed,signingConfigManager.getMyKey());

        /* Calculating fingerprint header */

        byte[] hash = null;
        messageDigest.reset();
        messageDigest.update(unhashedSign);
        hash = messageDigest.digest();

        /* Fingerprint verification */

        boolean flag = Arrays.equals(hash,fingerPrint);

        if(!flag) {
            throw new FingerprintVerificationException();
        }

    }

}
