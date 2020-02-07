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

@Component
public class SignatureConfigManager {

    Logger logger = LoggerFactory.getLogger(SignatureConfigManager.class);
    private boolean isDebugEnabled;

    private byte[] myKey;
    private String algorithm;

    @Value("${ffsec.signer.secret:#{null}}")
    private String secret;

    @Value("${ffsec.signer.algorithm:#{null}}")
    private String alg;

    @PostConstruct
    void init() throws Exception {

        isDebugEnabled = logger.isDebugEnabled();

        if(secret == null) {
            throw new Exception("You have to define the symmetric key into the property 'ffsec.signer.secret'");
        }
        myKey = secret.getBytes();

        if(isDebugEnabled) {
            logger.debug("Secret key correctly initialized");
        }

        if(Algorithms.contains(alg)) {
            this.algorithm = alg;
        } else {
            this.algorithm = Algorithms.valueOf("HmacSHA256").name();
        }

        if(isDebugEnabled) {
            logger.debug("Algorithm selected is {}", this.algorithm);
        }

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getMyKey() {
        return myKey;
    }

    public byte[] generateSignature(Mac mac, byte[] content) throws IOException {

        byte[] byteKey = getMyKey();
        SecretKeySpec keySpec = new SecretKeySpec(byteKey, getAlgorithm());
        try {
            mac.init(keySpec);
        } catch (InvalidKeyException e) {
            throw new IOException("Error during key initialization");
        }
        byte[] signature = mac.doFinal(content);

        if (isDebugEnabled) {
            logger.debug("Signature's generation finished");
        }

        return signature;

    }

}
