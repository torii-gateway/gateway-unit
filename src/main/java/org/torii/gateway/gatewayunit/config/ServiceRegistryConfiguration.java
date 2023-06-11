package org.torii.gateway.gatewayunit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.torii.gateway.gatewayunit.domain.Server;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RegisteredServiceConfiguration;

import java.util.List;


@Configuration
public class ServiceRegistryConfiguration {

    @Bean
    public List<RegisteredService> registeredServices() {

        List<Server> servers = List.of(Server.fromString("http://localhost:8081"), Server.fromString("http://localhost:8082"));

        return List.of(
                new RegisteredService("mock-service", RegisteredServiceConfiguration.builder()
                        .servers(servers)
                        .allowCache(false)
                        .build()),
                new RegisteredService("mock-service-cached", RegisteredServiceConfiguration.builder()
                        .servers(servers)
                        .allowCache(true)
                        .build())

        );
    }


}
