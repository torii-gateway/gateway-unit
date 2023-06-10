package org.torii.gateway.gatewayunit.domain;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Optional;

@Builder
public record RequestData(
        String serviceRef,
        String path,
        HttpMethod method,
        HttpHeaders headers,
        Optional<String> body
) {

}
