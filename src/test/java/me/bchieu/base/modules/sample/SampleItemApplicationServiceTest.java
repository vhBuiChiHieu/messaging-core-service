package me.bchieu.base.modules.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import me.bchieu.base.modules.sample.application.dto.CreateSampleItemCommand;
import me.bchieu.base.modules.sample.application.service.SampleItemApplicationService;
import me.bchieu.base.modules.sample.domain.model.SampleItem;
import me.bchieu.base.modules.sample.domain.repository.SampleItemRepository;
import me.bchieu.base.modules.sample.infrastructure.persistence.InMemorySampleItemRepository;
import org.junit.jupiter.api.Test;

class SampleItemApplicationServiceTest {

  @Test
  void shouldCreateAndReturnSampleItem() {
    SampleItemRepository repository = new InMemorySampleItemRepository();
    SampleItemApplicationService service =
        new SampleItemApplicationService(repository, () -> Instant.parse("2026-04-18T10:15:30Z"));

    SampleItem item = service.create(new CreateSampleItemCommand("starter item"));

    assertThat(item.id()).isEqualTo(1L);
    assertThat(item.name()).isEqualTo("starter item");
    assertThat(item.createdAt()).isEqualTo(Instant.parse("2026-04-18T10:15:30Z"));
    assertThat(repository.findById(1L)).contains(item);
  }
}
