package org.torii.gateway.gatewayunit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient webClient() {
        //the
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            clientRequest.method();
            clientRequest.url();
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers();
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            clientResponse.statusCode();
            log.info("Response: {}", clientResponse.statusCode());
            clientResponse.headers();
            clientResponse.headers().asHttpHeaders();
            HttpHeaders headers = clientResponse.headers().asHttpHeaders();
            headers.forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientResponse);
        });
    }



}
