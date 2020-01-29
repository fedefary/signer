package com.ffsecurity.signer.config;

import com.ffsecurity.signer.interceptors.ClientInterceptor;
import com.ffsecurity.signer.interceptors.ServerInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "com.ffsecurity")
@AutoConfigureAfter(value=RestTemplateAutoConfiguration.class)
@ConditionalOnClass(value=RestTemplateAutoConfiguration.class)
public class SignerConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private static ApplicationContext context;

    @Autowired
    private ServerInterceptor serverInterceptor;

    @Autowired
    private ClientInterceptor clientInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(serverInterceptor);
    }

    @PostConstruct
    private void configureRestTemplates() {
        Map<String,RestTemplate> restTemplatesMap = context.getBeansOfType(RestTemplate.class);
        if(!restTemplatesMap.isEmpty()) {
            restTemplatesMap.keySet().forEach(restTemplate -> {
                restTemplatesMap.get(restTemplate).getInterceptors().add(clientInterceptor);
            });
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SignerConfiguration.context = applicationContext;
    }
}
