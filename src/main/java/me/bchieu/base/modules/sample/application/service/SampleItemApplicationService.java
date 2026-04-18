package me.bchieu.base.modules.sample.application.service;

import java.time.Instant;
import java.util.function.Supplier;
import me.bchieu.base.modules.sample.application.dto.CreateSampleItemCommand;
import me.bchieu.base.modules.sample.domain.model.SampleItem;
import me.bchieu.base.modules.sample.domain.repository.SampleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SampleItemApplicationService {

  private final SampleItemRepository sampleItemRepository;
  private final Supplier<Instant> nowSupplier;

  @Autowired
  public SampleItemApplicationService(SampleItemRepository sampleItemRepository) {
    this(sampleItemRepository, Instant::now);
  }

  public SampleItemApplicationService(
      SampleItemRepository sampleItemRepository, Supplier<Instant> nowSupplier) {
    this.sampleItemRepository = sampleItemRepository;
    this.nowSupplier = nowSupplier;
  }

  public SampleItem create(CreateSampleItemCommand command) {
    SampleItem item =
        new SampleItem(sampleItemRepository.nextId(), command.name(), nowSupplier.get());
    return sampleItemRepository.save(item);
  }
}
