package org.torii.gateway.gatewayunit.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RequestData;
import org.torii.gateway.gatewayunit.domain.ServiceResponse;
import org.torii.gateway.gatewayunit.exception.ServiceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestService {

    private final WebClient webClient;
    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;
    private final Map<String, RegisteredService> registeredServices;
    private final CacheService cacheService;

    @Autowired
    public RequestService(WebClient webClient, ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory, List<RegisteredService> registeredServices, CacheService cacheService) {
        this.webClient = webClient;
        this.loadBalancerFactory = loadBalancerFactory;
        this.cacheService = cacheService;
        this.registeredServices = registeredServices.stream().collect(Collectors.toMap(RegisteredService::id, Function.identity()));
    }

    public Mono<ResponseEntity<String>> sendRequest(RequestData requestData) {

        RegisteredService registeredService = registeredServices.computeIfAbsent(requestData.serviceRef(), s -> {
            throw new ServiceNotFoundException(s);
        });

        if (registeredService.registeredServiceConfigurations().isAllowCache() && Objects.equals(requestData.method(), HttpMethod.GET)) {
            log.info("Cached request: {}", requestData);
            return cachedProcessing(requestData);
        } else {
            log.info("Non-cached request: {}", requestData);
            return nonCachedProcessing(requestData);
        }

    }

    public Mono<ResponseEntity<String>> cachedProcessing(RequestData requestData) {
        String key = String.valueOf(requestData.cacheKey());
        return cacheService.existsForKey(key)
                .flatMap(exists -> {
                    if (exists) {
                        return cacheService.get(key).map(mapCacheToResponse());
                    } else {
                        return processRequest(requestData)
                                .flatMap(response -> cacheResponse(key, response))
                                .map(mapCacheToResponse());
                    }
                });

    }

    private Mono<ServiceResponse> cacheResponse(String key, ResponseEntity<String> response) {
        ServiceResponse serviceResponse = ServiceResponse.from(
                response.getHeaders(),
                Objects.isNull(response.getBody()) ? "" : response.getBody()
        );
        return cacheService.save(key, serviceResponse, 60);
    }

    private static Function<ServiceResponse, ResponseEntity<String>> mapCacheToResponse() {
        return response -> ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
    }

    public Mono<ResponseEntity<String>> nonCachedProcessing(RequestData requestData) {
        return processRequest(requestData);
    }

    public Mono<ResponseEntity<String>> processRequest(RequestData requestData) {

        log.info("Sending request to service: {}", requestData.serviceRef());
        ReactiveLoadBalancer<ServiceInstance> ReactiveLoadBalancerInstance = loadBalancerFactory.getInstance(requestData.serviceRef());


        return Mono.just(ReactiveLoadBalancerInstance)
                .flatMap(loadBalancer -> Mono.from(loadBalancer.choose()))
                .map(Response::getServer)
                .map(instance -> createRequestBodySpec(instance, requestData))
                .flatMap(forwardRequestFunction(requestData));
    }

    private static ForwardRequestFunction forwardRequestFunction(RequestData requestData) {
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
