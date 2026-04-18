package me.bchieu.messaging.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import org.junit.jupiter.api.Test;

class MigrationStructureIntegrationTest {

  @Test
  void shouldExposeTimestampBasedInitialMigration() {
    URL initialSchemaMigration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0001__init_message_service_schema.sql");
    URL appUserMigration =
        getClass()
            .getClassLoader()
            .getResource("db/migration/V20260418_0002__create_app_user_table.sql");

    assertThat(initialSchemaMigration).isNotNull();
    assertThat(appUserMigration).isNotNull();
  }
}
