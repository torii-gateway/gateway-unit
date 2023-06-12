package org.torii.gateway.gatewayunit.service;

import org.springframework.stereotype.Service;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RegisteredServiceConfiguration;
import org.torii.gateway.gatewayunit.exception.ServiceAlreadyExistsException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class RegistryService {

    private final Map<String, RegisteredService> registeredServices = new ConcurrentHashMap<>();

    private final LoadBalancerService loadBalancerService;

    public RegistryService(LoadBalancerService loadBalancerService) {

        var server1 = new RegisteredServiceConfiguration.Server("http", "localhost", 8081);
        var server2 = new RegisteredServiceConfiguration.Server("http", "localhost", 8082);

        registeredServices.put(
                "s1", new RegisteredService("s1", RegisteredServiceConfiguration.builder()
                        .allowCache(false)
                        .server(server1)
                        .server(server2)
                        .build())
        );

        this.loadBalancerService = loadBalancerService;
    }

    public Mono<RegisteredService> get(String id) {
        return Mono.justOrEmpty(registeredServices.get(id));
    }

    public Mono<List<RegisteredService>> get() {
        return Mono.just(List.copyOf(registeredServices.values()));
    }

    public Mono<RegisteredService> add(RegisteredService registeredService) {

        registeredServices.computeIfPresent(registeredService.id(), (k, v) -> {
            throw new ServiceAlreadyExistsException((k));
        });

        registeredServices.put(registeredService.id(), registeredService);
        return Mono.just(registeredService);
    }

    public Mono<List<RegisteredService>> add(List<RegisteredService> registeredServices) {
        registeredServices.forEach(this::add);
        return Mono.just(registeredServices);
    }

    public Mono<RegisteredService> delete(String id) {
        return Mono.justOrEmpty(registeredServices.remove(id));
    }

    public Mono<RegisteredService> update(String id, RegisteredServiceConfiguration registeredServiceConfiguration) {
        return get(id).map(registeredService -> {
            new RegisteredService(id, registeredServiceConfiguration);
            return add(registeredService);
        }).flatMap(Function.identity()).doOnNext(
                registeredService -> loadBalancerService.refresh(Mono.just(registeredService))
        );

    }
}
