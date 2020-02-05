package com.ffsec.signer.interceptors;

import com.ffsec.signer.config.SignatureConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class ClientInterceptor implements ClientHttpRequestInterceptor {

    MessageDigest messageDigest;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @PostConstruct
    void initMessageDigest() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if("true".equalsIgnoreCase((String)req.getAttribute("sign")) && (request.getMethod().matches(HttpMethod.POST.name())
                || request.getMethod().matches(HttpMethod.PUT.name()) || request.getMethod().matches(HttpMethod.PATCH.name()))) {

            /* Generating random seed */

            byte[] randomSeed = signatureConfigManager.generateRandomSeed();

            /* Calculating xored key */

            byte[] xoredKey = signatureConfigManager.calculateXor(randomSeed, signatureConfigManager.getMyKey());

            /* Concatenating xor result with body byte array */

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            outputStream.write(xoredKey);
            outputStream.write(body);

            /* Calculating signature */

            messageDigest.reset();
            messageDigest.update(outputStream.toByteArray());
            byte[] hash = messageDigest.digest();

            request.getHeaders().add("Seed", Base64.getEncoder().encodeToString(randomSeed));
            request.getHeaders().add("Signature", Base64.getEncoder().encodeToString(hash));

        }

        return execution.execute(request,body);
    }
}
