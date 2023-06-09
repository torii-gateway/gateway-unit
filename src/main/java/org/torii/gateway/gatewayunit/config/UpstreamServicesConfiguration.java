package org.torii.gateway.gatewayunit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.torii.gateway.gatewayunit.domain.UpstreamService;

import java.util.List;

@Configuration
public class UpstreamServicesConfiguration {


    @Bean
    public List<UpstreamService> upstreamServices() {
        return List.of(
                new UpstreamService("service-1", "http://localhost:8081")
        );
    }


}
