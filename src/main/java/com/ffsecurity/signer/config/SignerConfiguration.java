package com.ffsecurity.signer.config;

import com.ffsecurity.signer.interceptors.ClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan(basePackages = "com.ffsecurity")
@AutoConfigureAfter(value=RestTemplateAutoConfiguration.class)
@ConditionalOnClass(value=RestTemplateAutoConfiguration.class)
public class SignerConfiguration implements WebMvcConfigurer {

    @Autowired
    private ClientInterceptor clientInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(clientInterceptor);
        return restTemplate;
    }

}
