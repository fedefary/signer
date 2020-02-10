package com.ffsec.signer.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;

/**
 * This aspect is used to tell to the {@link com.ffsec.signer.interceptors.ClientInterceptor}
 * that the request must be signed.
 *
 * @author Federico Farinetto
 */
@Component
@Aspect
public class SignAspect {


    @Before("@annotation(com.ffsec.signer.annotations.Sign)")
    public void makeHeader() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        request.setAttribute("sign","true");
    }


}
