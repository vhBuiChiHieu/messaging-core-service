package me.bchieu.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MessageServiceApplicationTests {

  @Test
  void shouldLoadMessageServiceApplicationContext() {
    assertThat(MessageServiceApplication.class).isNotNull();
  }
}
