package me.bchieu.messaging.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI baseOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Message Service API")
                .description("Backend message service modular monolith APIs")
                .version("v1"));
  }
}
