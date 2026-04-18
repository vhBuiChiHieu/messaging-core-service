package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

class MigrationStructureIntegrationTest {

  @Test
  void shouldExposeTimestampBasedInitialMigration() {
    URL migration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0001__init_message_service_schema.sql");

    assertThat(migration).isNotNull();
  }
}
