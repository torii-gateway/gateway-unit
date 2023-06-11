package org.torii.gateway.gatewayunit.domain;

import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;

@Data
public class ResponseData implements Serializable {
    private HttpHeaders headers;

    private String body;

    public static ResponseData from(HttpHeaders headers, String body) {
        ResponseData responseData = new ResponseData();
        responseData.setBody(body);
        responseData.setHeaders(headers);
        return responseData;
    }

}
