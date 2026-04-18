package me.bchieu.messaging.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"})
@AutoConfigureMockMvc
@ActiveProfiles("local")
class IdentityUserApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void shouldCreateUser() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "alice",
                      "displayName": "Alice",
                      "avatarUrl": "https://cdn.example.com/alice.png",
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("alice"))
        .andExpect(jsonPath("$.data.displayName").value("Alice"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void shouldGetUserByUsername() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "bob",
                      "displayName": "Bob",
                      "avatarUrl": null,
                      "status": "INACTIVE"
                    }
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/identity/users/by-username/{username}", "bob"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value("bob"))
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }
}
