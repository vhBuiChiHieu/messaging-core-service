package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import me.bchieu.messaging.common.security.CurrentIntegrationApp;
import me.bchieu.messaging.common.security.CurrentPrincipal;
import me.bchieu.messaging.common.security.SecurityEndpointProperties;
import org.junit.jupiter.api.Test;

class SecurityConfigurationSmokeTest {

  @Test
  void shouldExposeSecurityFoundationTypes() {
    assertThat(CurrentPrincipal.class).isNotNull();
    assertThat(CurrentIntegrationApp.class).isNotNull();
    assertThat(SecurityEndpointProperties.class).isNotNull();
  }
}
