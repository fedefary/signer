package com.ffsecurity.signer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
public class SigningConfigManager {

    /* Supported hash algorithms */
    public static final String MD5 = "MD5";
    public static final String SHA_1 = "SHA-1";
    public static final String SHA_256 = "SHA-256";

    private byte[] myKey;
    private String algorithm;
    private int size;

    @Value("${ffsecurity.signer.secret}")
    String secret;

    @Value("${ffsecurity.signer.algorithm:#{null}}")
    String alg;

    @PostConstruct
    void init() throws Exception {

        /* Init secret key */
        if(secret == null)
            throw new Exception("You have to define the symmetric key into the property 'ffsecurity.signer.secret'");
        myKey = secret.getBytes();

        /* Init MessageDigest */

        if(MD5.equalsIgnoreCase(alg) || SHA_1.equalsIgnoreCase(alg) || SHA_256.equalsIgnoreCase(alg))
            this.algorithm = alg;
        else this.algorithm = SHA_256;

        /* Init seed size */

        this.size = MD5.equalsIgnoreCase(algorithm) ? 16 : (SHA_1.equalsIgnoreCase(algorithm) ? 20 : (SHA_256.equalsIgnoreCase(algorithm) ? 32 : 0));

    }

    public byte[] generateRandomSeed() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[this.size];
        random.nextBytes(bytes);
        return bytes;
    }

    public byte[] calculateXor(byte[] seed, byte[] key) {
        byte[] xor = new byte[this.size];
        byte[] paddedKey = Arrays.copyOf(key,this.size);
        int i = 0;
        for (byte b : seed) {
            xor[i] = (byte) (b ^ paddedKey[i++]);
        }
        return xor;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public byte[] getMyKey() {
        return myKey;
    }
}
