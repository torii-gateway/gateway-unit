package org.torii.gateway.gatewayunit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.torii.gateway.gatewayunit.domain.RequestData;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
public class RequestService {

    private final WebClient webClient;
    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    @Autowired
    public RequestService(WebClient webClient, ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory) {
        this.webClient = webClient;
        this.loadBalancerFactory = loadBalancerFactory;
    }

    public Mono<ResponseEntity<String>> sendRequest(RequestData requestData) {

        log.info("Sending request to service: {}", requestData.serviceRef());

        return Mono.just(loadBalancerFactory.getInstance(requestData.serviceRef()))
                .flatMap(loadBalancer -> Mono.from(loadBalancer.choose()))
                .map(Response::getServer)
                .map(instance -> createRequestBodySpec(instance, requestData))
                .flatMap(forwardRequest(requestData));
    }

    private static ForwardRequestFunction forwardRequest(RequestData requestData) {
        return requestBodySpec -> requestData.body()
                .map(body -> requestBodySpec.bodyValue(body).exchangeToMono(response -> response.toEntity(String.class)))
                .orElseGet(() -> requestBodySpec.exchangeToMono(response -> response.toEntity(String.class)));
    }

    private WebClient.RequestBodySpec createRequestBodySpec(
            ServiceInstance instance,
            RequestData requestData
    ) {
        String uri = instance.getUri() + requestData.path();

        WebClient.RequestBodySpec requestSpec = webClient.method(requestData.method()).uri(uri);

        Optional.ofNullable(requestData.headers())
                .ifPresent(headers -> headers.forEach((key, value) -> requestSpec.header(key, value.toArray(new String[0]))));

        return requestSpec;
    }

    interface ForwardRequestFunction extends Function<WebClient.RequestBodySpec, Mono<? extends ResponseEntity<String>>> {
    }


}
