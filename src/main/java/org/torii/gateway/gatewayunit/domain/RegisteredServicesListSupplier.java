package org.torii.gateway.gatewayunit.domain;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RegisteredServicesListSupplier implements ServiceInstanceListSupplier {

    private final List<ServiceInstance> instances;
    private final String serviceId;

    public RegisteredServicesListSupplier(RegisteredService registeredService) {
        this.serviceId = registeredService.id();
        this.instances = registeredService.registeredServiceConfigurations().getServers().stream().map(server -> new DefaultServiceInstance(
                UUID.randomUUID().toString(),
                this.serviceId,
                server.host(),
                server.port(),
                false
        )).collect(Collectors.toList());
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(instances);
    }

    @Override
    public String getServiceId() {
        return this.serviceId;
    }

}
