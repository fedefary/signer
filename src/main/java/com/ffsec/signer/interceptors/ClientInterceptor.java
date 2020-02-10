package com.ffsec.signer.interceptors;

import com.ffsec.signer.config.SignatureConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

/**
 * This class provide an implementation of {@link ClientHttpRequestInterceptor}.
 * It is responsible of signature generation and Signature header creation.
 *
 * @author Federico Farinetto
 */
@Component
public class ClientInterceptor implements ClientHttpRequestInterceptor {

    Logger logger = LoggerFactory.getLogger(ClientInterceptor.class);

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        boolean isTraceEnabled = logger.isTraceEnabled();

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if("true".equalsIgnoreCase((String)req.getAttribute("sign"))) {

            req.removeAttribute("sign");

            byte[] signature;

            if(body.length > 0) {

                if (isTraceEnabled) {
                    logger.trace("The request contains a body, signature's generation started");
                }

                signature = signatureConfigManager.generateSignature(body);


            } else {

                if (isTraceEnabled) {
                    logger.trace("The request does not contain a body, using custom header");
                    logger.trace("Signature's generation started");
                }

                byte[] array = new byte[32];
                new Random().nextBytes(array);
                String generatedString = new String(array, StandardCharsets.UTF_8);

                signature = signatureConfigManager.generateSignature(generatedString.getBytes());

                request.getHeaders().add("Seed", Base64.getEncoder().encodeToString(generatedString.getBytes()));

            }

            request.getHeaders().add("Signature", Base64.getEncoder().encodeToString(signature));

            if (isTraceEnabled) {
                logger.trace("Signature's header attached to the request");
            }

        } else if(isTraceEnabled) {
            logger.trace("Nothing to sign");
        }

        return execution.execute(request,body);

    }
}
