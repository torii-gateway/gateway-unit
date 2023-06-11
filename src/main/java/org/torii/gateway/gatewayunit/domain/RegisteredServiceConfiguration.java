package org.torii.gateway.gatewayunit.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Builder
@Getter
public class RegisteredServiceConfiguration {
    @Singular
    private List<Server> servers;
    private boolean allowCache;
    private int ttl;

    public record Server (String protocol , String host, int port) {
    }

}
