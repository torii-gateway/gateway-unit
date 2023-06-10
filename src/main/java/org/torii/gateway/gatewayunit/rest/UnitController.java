package org.torii.gateway.gatewayunit.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer.Factory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UnitController {
    private final WebClient webClient;

    private final Factory<ServiceInstance> loadBalancerFactory;

    @RequestMapping(path = "/{serviceRef}/**", method = {GET, POST, PUT, DELETE})
    public Mono<ResponseEntity<String>> handleAllMethods(
            ServerHttpRequest request,
            @RequestBody(required = false) Mono<String> requestBody,
            @PathVariable String serviceRef
    ) {

        ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory.getInstance(serviceRef);


        String path = request.getPath()
                .pathWithinApplication()
                .value()
                .substring(serviceRef.length() + 1);


        HttpMethod method = request.getMethod();


        Flux<Mono<ResponseEntity<String>>> monoFlux = Flux.from(loadBalancer.choose()).map(i -> {

            String uri = i.getServer().getUri() + path;

            WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(uri);

            if (Objects.equals(method, HttpMethod.POST) || Objects.equals(method, HttpMethod.PUT)) {
                requestSpec.contentType(Objects.requireNonNull(request.getHeaders().getContentType()));
            }

            return requestBody.map(body -> requestSpec
                    .bodyValue(body)
                    .exchangeToMono(response -> response.toEntity(String.class))
            ).defaultIfEmpty(
                    requestSpec.exchangeToMono(response -> response.toEntity(String.class))
            );
        }).flatMap(Function.identity());


        return monoFlux.next().flatMap(Function.identity()).onErrorResume(ResponseStatusException.class, ex -> {
            log.error("Error occurred while handling request", ex);
            return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getReason()));
        });

    }

}
