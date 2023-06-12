package org.torii.gateway.gatewayunit.exception;

public class ServiceAlreadyExistsException extends RuntimeException {
    public ServiceAlreadyExistsException(String serviceId) {
        super("Service with id " + serviceId + " already exists");
    }
}
