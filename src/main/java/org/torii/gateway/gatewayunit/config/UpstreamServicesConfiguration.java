package org.torii.gateway.gatewayunit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.torii.gateway.gatewayunit.domain.Server;
import org.torii.gateway.gatewayunit.domain.UpstreamService;

import java.util.List;


@Configuration
public class UpstreamServicesConfiguration {

    @Bean
    public List<UpstreamService> upstreamServices() {
        return List.of(
                new UpstreamService("mock-service-1", List.of(Server.fromString("http://localhost:8081"), Server.fromString("http://localhost:8082"))),
                new UpstreamService("mock-service-2", List.of(Server.fromString("http://localhost:8083"), Server.fromString("http://localhost:8084")))
        );
    }


}
