package org.torii.gateway.gatewayunit.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.torii.gateway.gatewayunit.domain.RequestData;
import org.torii.gateway.gatewayunit.exception.Error;
import org.torii.gateway.gatewayunit.exception.ServiceNotFoundException;
import org.torii.gateway.gatewayunit.service.RequestService;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/services")
public class UnitController {

    private final RequestService requestService;

    @ResponseStatus(HttpStatus.NOT_FOUND) // 404 status code
    @ExceptionHandler(ServiceNotFoundException.class)
    public Mono<ResponseEntity<Error>> handleServiceNotFound(ServiceNotFoundException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage())));
    }

    @RequestMapping(path = "/{serviceRef}/**", method = {GET, POST, PUT, DELETE})
    public Mono<ResponseEntity<String>> handleAllMethods(
            ServerHttpRequest request,
            @RequestBody(required = false) Mono<String> requestBody,
            @PathVariable String serviceRef
    ) {

        var requestDataBuilder = RequestData.builder()
                .serviceRef(serviceRef)
                .path(extractPath(request, serviceRef))
                .method(request.getMethod())
                .headers(request.getHeaders());

        return requestBody
                .defaultIfEmpty("")
                .map(body -> requestDataBuilder.body(Optional.ofNullable(body.isEmpty() ? null : body)).build())
                .flatMap(requestService::sendRequest);

    }

    private static String extractPath(ServerHttpRequest request, String serviceRef) {
        return request.getPath()
                .pathWithinApplication()
                .value()
                .substring(serviceRef.length() + 1);
    }

}
