package org.torii.gateway.gatewayunit.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.SimpleObjectProvider;
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
    @Primary
    public ReactiveLoadBalancer.Factory<ServiceInstance> reactiveLoadBalancerFactory(List<UpstreamService> upstreamServices) {
        Map<String, UpstreamServicesListSupplier> serviceInstanceListSupplierMap = upstreamServices.stream().collect(Collectors.toMap(UpstreamService::id, UpstreamServicesListSupplier::new));
        return new ReactiveLoadBalancer.Factory<>() {
            @Override
            public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
                ObjectProvider<ServiceInstanceListSupplier> supplierProvider = new SimpleObjectProvider<>(serviceInstanceListSupplierMap.get(serviceId));
                return new RoundRobinLoadBalancer(supplierProvider, serviceId);
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
