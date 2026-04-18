package me.bchieu.base.modules.sample.infrastructure.persistence;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import me.bchieu.base.modules.sample.domain.model.SampleItem;
import me.bchieu.base.modules.sample.domain.repository.SampleItemRepository;
import org.springframework.stereotype.Repository;

@Repository
public class InMemorySampleItemRepository implements SampleItemRepository {

  private final Map<Long, SampleItem> storage = new LinkedHashMap<>();
  private long sequence = 0;

  @Override
  public SampleItem save(SampleItem item) {
    storage.put(item.id(), item);
    return item;
  }

  @Override
  public Optional<SampleItem> findById(Long id) {
    return Optional.ofNullable(storage.get(id));
  }

  @Override
  public long nextId() {
    sequence += 1;
    return sequence;
  }
}
