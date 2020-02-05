package com.ffsec.signer.interceptors;

import com.ffsec.signer.config.SignatureConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class ClientInterceptor implements ClientHttpRequestInterceptor {

    Mac mac;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @PostConstruct
    void initMac() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if("true".equalsIgnoreCase((String)req.getAttribute("sign")) && body != null && body.length > 0) {

            byte[] byteKey = signatureConfigManager.getMyKey();
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, signatureConfigManager.getAlgorithm());
            try {
                mac.init(keySpec);
            } catch (InvalidKeyException e) {
                throw new IOException("Error during key initialization");
            }
            byte[] signature = mac.doFinal(body);

            request.getHeaders().add("Signature", Base64.getEncoder().encodeToString(signature));

        }

        return execution.execute(request,body);
    }
}
