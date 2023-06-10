package org.torii.gateway.gatewayunit.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.torii.gateway.gatewayunit.domain.UpstreamService;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
public class LoadBalancerConfiguration {


    @Bean
    public Map<String, LeastConnectionsLoadBalancer> leastConnectionsLoadBalancerMap(List<UpstreamService> upstreamServices) {

        Map<String, UpstreamServicesListSupplier> serviceInstanceListSupplierMap = upstreamServices.stream().
                collect(Collectors.toMap(UpstreamService::id, UpstreamServicesListSupplier::new));

        return upstreamServices.stream()
                .collect(Collectors.toMap(UpstreamService::id, upstreamService -> new LeastConnectionsLoadBalancer(
                        serviceInstanceListSupplierMap.get(upstreamService.id()))));
    }

    @Bean
    @Primary
    public ReactiveLoadBalancer.Factory<ServiceInstance> reactiveLoadBalancerFactory(Map<String, LeastConnectionsLoadBalancer> leastConnectionsLoadBalancerMap) {
        return new ReactiveLoadBalancer.Factory<>() {
            @Override
            public LeastConnectionsLoadBalancer getInstance(String serviceId) {
                return leastConnectionsLoadBalancerMap.get(serviceId);
            }

            @Override
            public <X> Map<String, X> getInstances(String name, Class<X> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <X> X getInstance(String name, Class<?> clazz, Class<?>... generics) {
                throw new UnsupportedOperationException();
            }
        };

    }

    public static class UpstreamServicesListSupplier implements ServiceInstanceListSupplier {

        private final List<ServiceInstance> instances;
        private final String serviceId;

        public UpstreamServicesListSupplier(UpstreamService upstreamService) {
            this.serviceId = upstreamService.id();
            this.instances = upstreamService.servers().stream().map(server -> new DefaultServiceInstance(
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
}
