package org.torii.gateway.gatewayunit.service;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RegisteredServiceConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class LoadBalancerServiceTest {

    @Test
    void should_return_instance_of_load_balancer() {
        LoadBalancerService loadBalancerService = new LoadBalancerService();

        var servers = List.of(new RegisteredServiceConfiguration.Server("http", "localhost", 8080));


        RegisteredService registeredService = new RegisteredService("test", RegisteredServiceConfiguration.builder()
                .allowCache(false)
                .servers(servers)
                .build());

        Mono<ReactiveLoadBalancer<ServiceInstance>> result = loadBalancerService.get(Mono.just(registeredService));

        StepVerifier.create(result)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

    }

    @Test
    void should_return_same_instance_of_load_balancer() {
        LoadBalancerService loadBalancerService = new LoadBalancerService();

        var servers = List.of(new RegisteredServiceConfiguration.Server("http", "localhost", 8080));

        RegisteredService registeredService = new RegisteredService("test", RegisteredServiceConfiguration.builder()
                .allowCache(false)
                .servers(servers)
                .build());

        Mono<ReactiveLoadBalancer<ServiceInstance>> result1 = loadBalancerService.get(Mono.just(registeredService));

        Mono<ReactiveLoadBalancer<ServiceInstance>> result2 = loadBalancerService.get(Mono.just(registeredService));

        StepVerifier.create(result1)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        StepVerifier.create(result2)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        assertThat(result1.block()).isSameAs(result2.block());
    }
}