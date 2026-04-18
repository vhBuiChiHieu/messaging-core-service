package me.bchieu.messaging.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class SwaggerDocsProdIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldNotExposeOpenApiDocsInProdProfile() throws Exception {
    // Prod profile không được expose OpenAPI docs endpoint.
    mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
  }

  @Test
  void shouldNotExposeSwaggerUiInProdProfile() throws Exception {
    // Prod profile không được expose Swagger UI endpoint.
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
  }
}
