package com.ffsec.signer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Component
public class SignatureConfigManager {

    Logger logger = LoggerFactory.getLogger(SignatureConfigManager.class);

    private byte[] myKey;
    private String algorithm;

    @Value("${ffsec.signer.secret:#{null}}")
    private String secret;

    @Value("${ffsec.signer.algorithm:#{null}}")
    private String alg;

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

}
