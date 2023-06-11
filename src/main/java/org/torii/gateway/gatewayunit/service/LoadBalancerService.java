package org.torii.gateway.gatewayunit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.support.SimpleObjectProvider;
import org.springframework.stereotype.Service;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RegisteredServicesListSupplier;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerService {

    private final Map<RegisteredService, Mono<ReactiveLoadBalancer<ServiceInstance>>> loadBalancers = new ConcurrentHashMap<>();

    public Mono<ReactiveLoadBalancer<ServiceInstance>> get(Mono<RegisteredService> registeredService) {

        return registeredService
                .flatMap(
                        service -> {
                            if (loadBalancers.containsKey(service)) {
                                return loadBalancers.get(service);
                            } else {
                                return buildLoadBalancer(registeredService)
                                        .doOnNext(lb -> loadBalancers.put(service, Mono.just(lb)));
                            }
                        }
                );
    }

    private Mono<RoundRobinLoadBalancer> buildLoadBalancer(Mono<RegisteredService> registeredService) {
        return registeredService
                .map(RegisteredServicesListSupplier::new)
                .map(service -> new RoundRobinLoadBalancer(new SimpleObjectProvider<>(service), service.getServiceId()));
    }

    private Mono<RegisteredService> createLoadBalancer(Mono<RegisteredService> registeredService) {
        return registeredService
                .flatMap(
                        service -> {
                            if (loadBalancers.containsKey(service)) {
                                return Mono.just(service);
                            } else {
                                return Mono.empty();
                            }
                        }
                );
    }

    public void refresh(Mono<RegisteredService> registeredService) {
         registeredService
                .flatMap(
                        service -> {
                            if (loadBalancers.containsKey(service)) {
                                return createLoadBalancer(registeredService);
                            } else {
                                return Mono.empty();
                            }
                        }
                ).subscribe(
                        s-> log.info("Load balancer refreshed for service: {}", s.id())
                );
    }


}
