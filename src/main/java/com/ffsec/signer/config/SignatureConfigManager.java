package com.ffsec.signer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class SignatureConfigManager {

    Logger logger = LoggerFactory.getLogger(SignatureConfigManager.class);

    private boolean isTraceEnabled;
    private boolean isErrorEnabled;

    private byte[] myKey;
    private String algorithm;

    @Value("${ffsec.signer.secret:#{null}}")
    private String secret;

    @Value("${ffsec.signer.algorithm:#{null}}")
    private String alg;

    @PostConstruct
    void init() throws Exception {

        isTraceEnabled = logger.isTraceEnabled();
        isErrorEnabled = logger.isErrorEnabled();

        if(secret == null) {
            throw new Exception("You have to define the symmetric key into the property 'ffsec.signer.secret'");
        }
        myKey = secret.getBytes();

        if(isTraceEnabled) {
            logger.trace("Secret key correctly initialized");
        }

        if(Algorithms.contains(alg)) {
            this.algorithm = alg;
        } else {
            this.algorithm = Algorithms.valueOf("HmacSHA256").name();
        }

        if(isTraceEnabled) {
            logger.trace("Algorithm selected is {}", this.algorithm);
        }

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getMyKey() {
        return myKey;
    }

    public byte[] generateSignature(byte[] content) throws IOException {

        Mac mac = null;
        try {
            mac = Mac.getInstance(getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            if (isErrorEnabled) {
                logger.error("Error occured during Mac creation");
            }
        }

        byte[] byteKey = getMyKey();
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, getAlgorithm());
        try {
            mac.init(keySpec);
        } catch (InvalidKeyException e) {
            throw new IOException("Error occured during key initialization");
        }
        byte[] signature = mac.doFinal(content);

        if (isErrorEnabled) {
            logger.error("Signature's generation finished");
        }

        return signature;

    }

}
