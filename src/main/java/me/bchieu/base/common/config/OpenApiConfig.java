package me.bchieu.base.common.config;

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
                .title("Base Service API")
                .description("Modular monolith base service starter APIs")
                .version("v1"));
  }
}
