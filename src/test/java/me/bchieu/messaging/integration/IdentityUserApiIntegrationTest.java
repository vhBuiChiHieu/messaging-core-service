package me.bchieu.messaging.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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

  @Test
  void shouldUpdateUser() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/identity/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "username": "charlie",
                          "displayName": "Charlie",
                          "avatarUrl": "https://cdn.example.com/charlie.png",
                          "status": "ACTIVE"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn();

    String userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(
            put("/api/v1/identity/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "charlie-updated",
                      "displayName": "Charlie Updated",
                      "avatarUrl": null,
                      "status": "INACTIVE"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(userId))
        .andExpect(jsonPath("$.data.username").value("charlie-updated"))
        .andExpect(jsonPath("$.data.displayName").value("Charlie Updated"))
        .andExpect(jsonPath("$.data.status").value("INACTIVE"));
  }

  @Test
  void shouldGetUserById() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/identity/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "username": "diana",
                          "displayName": "Diana",
                          "avatarUrl": "https://cdn.example.com/diana.png",
                          "status": "ACTIVE"
                        }
                        """))
            .andExpect(status().isOk())
            .andReturn();

    String userId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(get("/api/v1/identity/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(userId))
        .andExpect(jsonPath("$.data.username").value("diana"))
        .andExpect(jsonPath("$.data.displayName").value("Diana"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
  }

  @Test
  void shouldReturnConflictWhenCreateDuplicateUsername() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "duplicate-user",
                      "displayName": "First",
                      "avatarUrl": null,
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/identity/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "duplicate-user",
                      "displayName": "Second",
                      "avatarUrl": null,
                      "status": "INACTIVE"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value("Identity user already exists for username: duplicate-user"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void shouldReturnNotFoundWhenGetByMissingId() throws Exception {
    String missingUserId = "11111111-1111-1111-1111-111111111111";

    mockMvc
        .perform(get("/api/v1/identity/users/{userId}", missingUserId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value("Identity user not found for id: 11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  @Test
  void shouldReturnNotFoundWhenUpdateMissingId() throws Exception {
    String missingUserId = "22222222-2222-2222-2222-222222222222";

    mockMvc
        .perform(
            put("/api/v1/identity/users/{userId}", missingUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "missing-user",
                      "displayName": "Missing User",
                      "avatarUrl": null,
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(
            jsonPath("$.message")
                .value("Identity user not found for id: 22222222-2222-2222-2222-222222222222"))
        .andExpect(jsonPath("$.data").doesNotExist());
  }
}
