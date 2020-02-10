package com.ffsec.signer.config;

import com.ffsec.signer.interceptors.ClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Library's configuration class.
 * It contains the instantiation's method of the {@link RestTemplate} instance used by the library.
 *
 * @author Federico Farinetto
 */
@Configuration
@ComponentScan(basePackages = "com.ffsec")
@AutoConfigureAfter(value=RestTemplateAutoConfiguration.class)
@ConditionalOnClass(value=RestTemplateAutoConfiguration.class)
public class SignerConfiguration {

    @Autowired
    private ClientInterceptor clientInterceptor;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(clientInterceptor);
        return restTemplate;
    }

}
