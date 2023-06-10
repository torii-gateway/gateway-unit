package org.torii.gateway.gatewayunit.domain;

import java.util.regex.Pattern;

public record Server (String protocol , String host, int port) {

    public static final String SERVER_REGEX = "^(?<protocol>http|https)://(?<host>[^:]+):(?<port>\\d+)$";

    public static Server fromString(String server) {

        var matcher = Pattern.compile(SERVER_REGEX).matcher(server);

        if (matcher.matches()) {
            return new Server(matcher.group("protocol"), matcher.group("host"), Integer.parseInt(matcher.group("port")));
        } else {
            throw new IllegalArgumentException("Invalid server string: " + server);
        }

    }

}
