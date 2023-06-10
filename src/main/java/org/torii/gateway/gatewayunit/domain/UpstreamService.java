package org.torii.gateway.gatewayunit.domain;


import java.util.List;

public record UpstreamService(String id, List<Server> servers) {
}
