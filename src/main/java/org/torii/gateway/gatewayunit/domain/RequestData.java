package org.torii.gateway.gatewayunit.domain;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Objects;
import java.util.Optional;

@Builder
public record RequestData(
        String serviceRef,
        String path,
        HttpMethod method,
        HttpHeaders headers,
        Optional<String> body
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestData that = (RequestData) o;
        return serviceRef.equals(that.serviceRef) && path.equals(that.path) && method.equals(that.method) && headers.equals(that.headers) && body.equals(that.body);
    }

    public int cacheKey() {
        return Objects.hash(serviceRef, path, method, body);
    }
}
