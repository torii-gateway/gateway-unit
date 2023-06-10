package org.torii.gateway.gatewayunit.config;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class LeastConnectionsLoadBalancer implements ReactiveLoadBalancer<ServiceInstance> {

    private final ConcurrentHashMap<ServiceInstance, AtomicInteger> instanceConnections;

    public LeastConnectionsLoadBalancer(ServiceInstanceListSupplier serviceInstanceListSupplier) {
        this.instanceConnections = new ConcurrentHashMap<>();
        serviceInstanceListSupplier.get()
                .subscribe(mapInstances());
    }

    private Consumer<List<ServiceInstance>> mapInstances() {
        return serviceInstance -> serviceInstance.forEach(instance -> instanceConnections.put(instance, new AtomicInteger(0)));
    }

    @Override
    public Publisher<Response<ServiceInstance>> choose(Request request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Response<ServiceInstance>> choose() {
        ServiceInstance chosenInstance = null;
        int leastConnections = Integer.MAX_VALUE;

        for (Map.Entry<ServiceInstance, AtomicInteger> entry : instanceConnections.entrySet()) {
            int currentConnections = entry.getValue().get();
            if (currentConnections < leastConnections) {
                leastConnections = currentConnections;
                chosenInstance = entry.getKey();
            }
        }

        if (chosenInstance != null) {
            instanceConnections.get(chosenInstance).incrementAndGet();
        }

        return Mono.just(new DefaultResponse(chosenInstance));
    }

    public void connectionClosed(ServiceInstance instance) {
        instanceConnections.get(instance).decrementAndGet();
    }
}
