package org.torii.gateway.gatewayunit.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RegisteredServiceConfiguration {
    private List<Server> servers;
    private boolean allowCache;
}
