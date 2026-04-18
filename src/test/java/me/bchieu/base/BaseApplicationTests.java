package me.bchieu.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BaseApplicationTests {

  @Test
  void contextLoads() {}

  @Test
  void shouldUseSpringBoot35Baseline() {
    // Khóa baseline để tránh lệch major/minor ngoài 3.5.x.
    assertThat(SpringBootVersion.getVersion()).startsWith("3.5.");
  }
}
