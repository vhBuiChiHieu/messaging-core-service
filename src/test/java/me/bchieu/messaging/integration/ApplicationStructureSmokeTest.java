package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationStructureSmokeTest {

  @Test
  void shouldIncludeMessageServiceConfigurationResourcesOnClasspath() {
    assertThat(getClass().getClassLoader().getResource("application.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-local.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-dev.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-prod.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("logback-spring.xml")).isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("db/migration/V20260418_0001__init_message_service_schema.sql"))
        .isNotNull();
  }

  @Test
  void shouldIncludeCorePackageSkeletonTypes() throws ClassNotFoundException {
    assertThat(Class.forName("me.bchieu.messaging.common.config.MessagingPackageMarker"))
        .isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.common.exception.GlobalExceptionHandler"))
        .isNotNull();
    assertThat(Class.forName("me.bchieu.messaging.common.response.ApiResponse")).isNotNull();
    assertThat(
            Class.forName(
                "me.bchieu.messaging.infrastructure.persistence.PersistencePackageMarker"))
        .isNotNull();
  }

  @Test
  void shouldExposeOfficialMessagingModules() {
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/identity/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/conversation/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/message/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/media/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/integration/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/realtime/package-info.class"))
        .isNotNull();
    assertThat(
            getClass()
                .getClassLoader()
                .getResource("me/bchieu/messaging/modules/notification/package-info.class"))
        .isNotNull();
  }
}
