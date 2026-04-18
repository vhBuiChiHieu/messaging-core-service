package me.bchieu.base.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class SwaggerDocsLocalIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldExposeOpenApiDocsInLocalProfile() throws Exception {
    // Local profile phải expose OpenAPI docs endpoint để phục vụ dev/test API.
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"));
  }

  @Test
  void shouldExposeSwaggerUiInLocalProfile() throws Exception {
    // Springdoc thường redirect /swagger-ui.html về đường dẫn UI thực tế.
    mockMvc
        .perform(get("/swagger-ui.html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(header().string("Location", "/swagger-ui/index.html"));
  }
}
