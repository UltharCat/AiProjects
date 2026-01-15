package com.ai.alibaba.config;

import com.ai.alibaba.client.AiWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class WebConfig {

    @Bean
    public AiWebClient aiWebClient() {
        String baseUrl = "http://localhost:18081";
        return HttpServiceProxyFactory.builderFor(
                WebClientAdapter.create(
                        WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/xxx-www-form-urlencoded")
                                .build()
                )
        ).build().createClient(AiWebClient.class);
    }

}
