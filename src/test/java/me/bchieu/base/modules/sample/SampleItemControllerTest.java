package me.bchieu.base.modules.sample;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import me.bchieu.base.modules.sample.api.SampleItemController;
import me.bchieu.base.modules.sample.application.service.SampleItemApplicationService;
import me.bchieu.base.modules.sample.domain.model.SampleItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SampleItemController.class)
class SampleItemControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SampleItemApplicationService sampleItemApplicationService;

  @Test
  void shouldReturnSuccessEnvelopeWhenCreateSampleItem() throws Exception {
    when(sampleItemApplicationService.create(any()))
        .thenReturn(new SampleItem(1L, "starter item", Instant.parse("2026-04-18T10:15:30Z")));

    mockMvc
        .perform(
            post("/api/v1/sample-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "starter item"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").isEmpty())
        .andExpect(jsonPath("$.data.id").value(1L))
        .andExpect(jsonPath("$.data.name").value("starter item"));
  }

  @Test
  void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/sample-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": ""
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("name: must not be blank"));
  }
}
