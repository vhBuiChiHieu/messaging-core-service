package me.bchieu.base.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationStructureSmokeTest {

  @Test
  void shouldIncludeBaseConfigurationResourcesOnClasspath() {
    assertThat(getClass().getClassLoader().getResource("application.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-local.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-dev.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("application-prod.yml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("logback-spring.xml")).isNotNull();
    assertThat(getClass().getClassLoader().getResource("db/migration/V1__init_schema.sql"))
        .isNotNull();
    assertThat(getClass().getClassLoader().getResource("db/migration/V2__seed_default_data.sql"))
        .isNotNull();
  }

  @Test
  void shouldIncludeCorePackageSkeletonTypes() throws ClassNotFoundException {
    assertThat(Class.forName("me.bchieu.base.common.config.BasePackageMarker")).isNotNull();
    assertThat(Class.forName("me.bchieu.base.common.exception.GlobalExceptionHandler")).isNotNull();
    assertThat(Class.forName("me.bchieu.base.common.response.ApiResponse")).isNotNull();
    assertThat(Class.forName("me.bchieu.base.infrastructure.cache.CachePackageMarker")).isNotNull();
    assertThat(Class.forName("me.bchieu.base.infrastructure.persistence.PersistencePackageMarker"))
        .isNotNull();
  }
}
