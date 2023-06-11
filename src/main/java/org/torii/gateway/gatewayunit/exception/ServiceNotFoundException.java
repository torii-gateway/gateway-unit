package org.torii.gateway.gatewayunit.exception;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String serviceId) {
        super("Service with id " + serviceId + " not found");
    }
}
