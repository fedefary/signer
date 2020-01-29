package com.ffsecurity.signer.interceptors;

import com.ffsecurity.signer.config.SigningConfigManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;


@Component
public class ServerInterceptor implements HandlerInterceptor {

    MessageDigest messageDigest;

    @Autowired
    SigningConfigManager signingConfigManager;

    @PostConstruct
    void initMessageDigest() throws NoSuchAlgorithmException {
        messageDigest = MessageDigest.getInstance(signingConfigManager.getAlgorithm());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String fingerPrintHeader = request.getHeader("Fingerprint");
        String seedHeader = request.getHeader("Seed");

        if(fingerPrintHeader == null || seedHeader == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("You're trying to call authenticated service, please tell me who you are!");
            return false;
        }

        byte[] fingerPrint = Base64.getDecoder().decode(fingerPrintHeader);
        byte[] seed = Base64.getDecoder().decode(seedHeader);
        byte[] body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())).getBytes();

        /* Calculating salt */

        byte[] salt = signingConfigManager.calculateXor(seed,signingConfigManager.getMyKey());

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

        /* Fingerprint verification */

        boolean flag = Arrays.equals(hash,fingerPrint);

        if(!flag) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("You're trying to call authenticated service, please tell me who you are!");
        }

        return flag;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
