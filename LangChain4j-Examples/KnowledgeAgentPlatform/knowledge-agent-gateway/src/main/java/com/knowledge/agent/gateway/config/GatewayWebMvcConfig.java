package com.knowledge.agent.gateway.config;

import com.knowledge.agent.gateway.auth.GatewayAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers Gateway-specific MVC behaviors such as request authentication.
 */
@Configuration
public class GatewayWebMvcConfig implements WebMvcConfigurer {

    private final GatewayAuthInterceptor gatewayAuthInterceptor;

    public GatewayWebMvcConfig(GatewayAuthInterceptor gatewayAuthInterceptor) {
        this.gatewayAuthInterceptor = gatewayAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gatewayAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
