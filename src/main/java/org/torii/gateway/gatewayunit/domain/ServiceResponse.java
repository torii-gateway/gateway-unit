package org.torii.gateway.gatewayunit.domain;

import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;

@Data
public class ServiceResponse implements Serializable {
    private HttpHeaders headers;

    private String body;

    public static ServiceResponse from(HttpHeaders headers, String body) {
        ServiceResponse serviceResponse = new ServiceResponse();
        serviceResponse.setBody(body);
        serviceResponse.setHeaders(headers);
        return serviceResponse;
    }

}
