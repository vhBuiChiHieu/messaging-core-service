package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IdentityModuleStructureIntegrationTest {

  @Test
  void shouldPlaceIdentityRequestAndResponseDtosInDedicatedApiPackages()
      throws ClassNotFoundException {
    assertThat(
            Class.forName("me.bchieu.messaging.modules.identity.api.request.CreateUserRequest"))
        .isNotNull();
    assertThat(
            Class.forName("me.bchieu.messaging.modules.identity.api.request.UpdateUserRequest"))
        .isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.modules.identity.api.response.UserResponse"))
        .isNotNull();
  }
}
