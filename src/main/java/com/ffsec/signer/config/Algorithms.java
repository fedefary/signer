package com.ffsec.signer.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum that contains supported HMAC types.
 *
 * @author Federico Farinetto
 */
public enum Algorithms {

    HmacMD5,
    HmacSHA1,
    HmacSHA256,
    HmacSHA384,
    HmacSHA512;

    private static List<String> algorithms = new ArrayList<>();

    static {
        for(Algorithms alg : Algorithms.values()) {
            algorithms.add(alg.name());
        }
    }

    public static boolean contains(String algorithm) {
        return algorithms.stream().anyMatch(elem -> elem.equalsIgnoreCase(algorithm));
    }

}
