package org.torii.gateway.gatewayunit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RequestData;
import org.torii.gateway.gatewayunit.domain.ResponseData;
import org.torii.gateway.gatewayunit.exception.ServiceNotFoundException;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {

    private final WebClient webClient;
    private final LoadBalancerService loadBalancerService;
    private final CacheService cacheService;
    private final RegistryService registeredServiceProvider;


    public Mono<ResponseEntity<String>> sendRequest(RequestData requestData) {

        return registeredServiceProvider.get(requestData.serviceRef())
                .switchIfEmpty(Mono.error(new ServiceNotFoundException(requestData.serviceRef())))
                .flatMap(
                        registeredService -> {
                            if (canCache(requestData, registeredService)) {
                                return cachedProcessing(requestData , registeredService);
                            } else {
                                return nonCachedProcessing(requestData, registeredService);
                            }
                        }
                );
    }

    private static boolean canCache(RequestData requestData, RegisteredService registeredService) {
        return registeredService.registeredServiceConfigurations().isAllowCache() && Objects.equals(requestData.method(), HttpMethod.GET);
    }

    private Mono<ResponseEntity<String>> cachedProcessing(RequestData requestData, RegisteredService registeredService) {
        log.info("Cached request: {}", requestData);
        String key = String.valueOf(requestData.cacheKey());
        return cacheService.existsForKey(key)
                .flatMap(exists -> {
                    if (exists) {
                        return cacheService.get(key).map(mapCacheToResponse());
                    } else {
                        return processRequest(requestData, registeredService)
                                .flatMap(response -> cacheResponse(key, response , registeredService.registeredServiceConfigurations().getTtl())
                                .map(mapCacheToResponse()));
                    }
                });
    }

    private Mono<ResponseData> cacheResponse(String key, ResponseEntity<String> response , int ttl) {
        ResponseData responseData = ResponseData.from(
                response.getHeaders(),
                Objects.isNull(response.getBody()) ? "" : response.getBody()
        );
        return cacheService.save(key, responseData, ttl);
    }

    private static Function<ResponseData, ResponseEntity<String>> mapCacheToResponse() {
        return response -> ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
    }

    private Mono<ResponseEntity<String>> nonCachedProcessing(RequestData requestData, RegisteredService registeredService) {
        log.info("Non-cached request: {}", requestData);
        return processRequest(requestData, registeredService);
    }

    private Mono<ResponseEntity<String>> processRequest(RequestData requestData, RegisteredService registeredService) {

        log.info("Sending request to service: {}", requestData.serviceRef());

        return loadBalancerService.get(Mono.just(registeredService))
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
