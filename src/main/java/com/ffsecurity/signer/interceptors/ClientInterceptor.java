package com.ffsecurity.signer.interceptors;

import com.ffsecurity.signer.config.SigningConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class ClientInterceptor implements ClientHttpRequestInterceptor {

    MessageDigest messageDigest;

    @Autowired
    SigningConfigManager signingConfigManager;

    @PostConstruct
    void initMessageDigest() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(signingConfigManager.getAlgorithm());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        /* Generating random seed */

        byte[] randomSeed = signingConfigManager.generateRandomSeed();

        /* Calculating salt */

        byte[] salt = signingConfigManager.calculateXor(randomSeed,signingConfigManager.getMyKey());

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

        request.getHeaders().add("Seed", Base64.getEncoder().encodeToString(randomSeed));
        request.getHeaders().add("Fingerprint", Base64.getEncoder().encodeToString(hash));

        return execution.execute(request,body);
    }
}
