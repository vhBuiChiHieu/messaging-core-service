package me.bchieu.messaging.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityEndpointProperties(
    List<String> publicEndpoints, List<String> userEndpoints, List<String> integrationEndpoints) {

  public SecurityEndpointProperties {
    publicEndpoints = List.copyOf(publicEndpoints == null ? List.of() : publicEndpoints);
    userEndpoints = List.copyOf(userEndpoints == null ? List.of() : userEndpoints);
    integrationEndpoints =
        List.copyOf(integrationEndpoints == null ? List.of() : integrationEndpoints);
  }
}
