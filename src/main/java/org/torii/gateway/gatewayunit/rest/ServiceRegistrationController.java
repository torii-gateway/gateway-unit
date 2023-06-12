package org.torii.gateway.gatewayunit.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.torii.gateway.gatewayunit.domain.RegisteredService;
import org.torii.gateway.gatewayunit.domain.RegisteredServiceConfiguration;
import org.torii.gateway.gatewayunit.service.RegistryService;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/service-registration")
@RequiredArgsConstructor
public class ServiceRegistrationController {

    private final RegistryService registryService;

    @GetMapping
    public Mono<ResponseEntity<List<RegisteredService>>> get() {
        return registryService.get().map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<RegisteredService>> get(@PathVariable String id) {
        return registryService.get(id).map(ResponseEntity::ok).defaultIfEmpty(ResponseEntity.noContent().build());
    }

    @PostMapping
    public Mono<ResponseEntity<RegisteredService>> add(@RequestBody RegisteredService registeredService) {
        return registryService.add(registeredService).map(r -> {
            URI uri = URI.create("/service-registration/" + r.id());
            return ResponseEntity.created(uri).body(r);
        });
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<RegisteredService>> update(@PathVariable String id, @RequestBody RegisteredServiceConfiguration registeredServiceConfiguration) {
        return registryService.update(id, registeredServiceConfiguration).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<RegisteredService>> delete(@PathVariable String id) {
        return registryService.delete(id).map(registeredService ->
            ResponseEntity.accepted().build()
        );
    }

}
