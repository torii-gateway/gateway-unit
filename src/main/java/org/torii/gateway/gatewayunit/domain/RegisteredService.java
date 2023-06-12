package org.torii.gateway.gatewayunit.domain;


import java.util.Objects;

public record RegisteredService(String id, RegisteredServiceConfiguration registeredServiceConfigurations) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisteredService that = (RegisteredService) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
