package org.torii.gateway.gatewayunit.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.torii.gateway.gatewayunit.domain.UpstreamService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UnitController {
    private final WebClient webClient;

    private final List<UpstreamService> upstreamUpstreamServices;

    @RequestMapping(path = "/{serviceRef}/**", method = {GET, POST, PUT, DELETE})
    public Mono<ResponseEntity<String>> handleAllMethods(
            ServerHttpRequest request,
            @RequestBody(required = false) Mono<String> requestBody,
            @PathVariable String serviceRef
    ) {

        if (upstreamUpstreamServices.stream().noneMatch(s -> s.id().equals(serviceRef))) {
            return Mono.error(
                    new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Upstream service not found")
            );
        }

        String path = request.getPath()
                .pathWithinApplication()
                .value()
                .substring(serviceRef.length() + 1);

        String uri = upstreamUpstreamServices.stream()
                .filter(s -> s.id().equals(serviceRef))
                .findFirst()
                .map(UpstreamService::host)
                .map(url -> url + path)
                .orElseThrow();

        HttpMethod method = request.getMethod();

        WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(uri);

        if (Objects.equals(method, HttpMethod.POST) || Objects.equals(method, HttpMethod.PUT)) {
            requestSpec.contentType(Objects.requireNonNull(request.getHeaders().getContentType()));
        }

        Mono<Mono<ResponseEntity<String>>> monoMono = requestBody.map(body -> requestSpec
                .bodyValue(body)
                .exchangeToMono(response -> response.toEntity(String.class))
        ).defaultIfEmpty(
                requestSpec
                        .exchangeToMono(response -> response.toEntity(String.class))
        );

        return monoMono.flatMap(mono -> mono)
                .onErrorResume(ResponseStatusException.class, ex -> {
                    log.error("Error occurred while handling request", ex);
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getReason()));
                });
    }

}
