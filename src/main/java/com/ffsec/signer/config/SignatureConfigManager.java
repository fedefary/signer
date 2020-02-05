package com.ffsec.signer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class SignatureConfigManager {

    Logger logger = LoggerFactory.getLogger(SignatureConfigManager.class);

    public static final String MD5 = "HmacMD5";
    public static final String SHA_1 = "HmacSHA1";
    public static final String SHA_256 = "HmacSHA256";

    private byte[] myKey;
    private String algorithm;
    private int size;

    @Value("${ffsec.signer.secret:#{null}}")
    String secret;

    @Value("${ffsec.signer.algorithm:#{null}}")
    String alg;

    @PostConstruct
    void init() throws Exception {

        boolean isDebugEnabled = logger.isDebugEnabled();

        if(secret == null) {
            throw new Exception("You have to define the symmetric key into the property 'ffsec.signer.secret'");
        }
        myKey = secret.getBytes();

        if(isDebugEnabled) {
            logger.debug("Secret key correctly initialized");
        }

        if(MD5.equalsIgnoreCase(alg) || SHA_1.equalsIgnoreCase(alg) || SHA_256.equalsIgnoreCase(alg)) {
            this.algorithm = alg;
        } else {
            this.algorithm = SHA_256;
        }

        if(isDebugEnabled) {
            logger.debug("Algorithm selected is {}", this.algorithm);
        }

        this.size = MD5.equalsIgnoreCase(algorithm) ? 16 : (SHA_1.equalsIgnoreCase(algorithm) ? 20 : (SHA_256.equalsIgnoreCase(algorithm) ? 32 : 0));

    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getMyKey() {
        return myKey;
    }
}
