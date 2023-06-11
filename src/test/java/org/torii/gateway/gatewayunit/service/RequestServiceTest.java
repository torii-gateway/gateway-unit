package org.torii.gateway.gatewayunit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.torii.gateway.gatewayunit.domain.RequestData;
import org.torii.gateway.gatewayunit.exception.ServiceNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RequestServiceTest {

    private RequestService requestService;
    private RegistryService registryService;

    @BeforeEach
    void setUp() {
        WebClient webClient = Mockito.mock(WebClient.class);
        LoadBalancerService loadBalancerService = Mockito.mock(LoadBalancerService.class);
        CacheService cacheService = Mockito.mock(CacheService.class);
        registryService = Mockito.mock(RegistryService.class);
        requestService = new RequestService(webClient, loadBalancerService, cacheService, registryService);
    }

    @Test
    void should_throw_service_not_found_exception() {
        when(registryService.get(any())).thenReturn(Mono.empty());

        Mono<ResponseEntity<String>> response = requestService.sendRequest(
                new RequestData("test", "/test", HttpMethod.GET, new HttpHeaders(), Optional.empty()));

        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof ServiceNotFoundException)
                .verify();
    }



}
