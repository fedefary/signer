package com.ffsec.signer.interceptors;

import com.ffsec.signer.config.SignatureConfigManager;
import com.ffsec.signer.utils.SignerUtils;
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
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Component
public class ClientInterceptor implements ClientHttpRequestInterceptor {

    Logger logger = LoggerFactory.getLogger(ClientInterceptor.class);

    private Mac mac;

    @Autowired
    SignatureConfigManager signatureConfigManager;

    @PostConstruct
    void initMac() throws NoSuchAlgorithmException {
        mac = Mac.getInstance(signatureConfigManager.getAlgorithm());
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        boolean isDebugEnabled = logger.isDebugEnabled();

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        if("true".equalsIgnoreCase((String)req.getAttribute("sign"))) {

            req.removeAttribute("sign");

            byte[] signature = null;

            if(body != null && body.length > 0) {

                if (isDebugEnabled) {
                    logger.debug("The request contains a body, signature's generation started");
                }

                signature = signatureConfigManager.generateSignature(mac,body);


            } else if(!req.getParameterMap().isEmpty()) {

                if (isDebugEnabled) {
                    logger.debug("The request does not contain a body but only parameters, signature's generation started");
                }

                byte[] paramsByteArray = SignerUtils.convertRequestParameters(req.getParameterMap());

                signature = signatureConfigManager.generateSignature(mac,paramsByteArray);

            }

            request.getHeaders().add("Signature", Base64.getEncoder().encodeToString(signature));

            if (isDebugEnabled) {
                logger.debug("Signature's header attached to the request");
            }

        }

        return execution.execute(request,body);

    }
}
