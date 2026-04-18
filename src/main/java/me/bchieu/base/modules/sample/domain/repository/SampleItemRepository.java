package me.bchieu.base.modules.sample.domain.repository;

import java.util.Optional;
import me.bchieu.base.modules.sample.domain.model.SampleItem;

public interface SampleItemRepository {

  SampleItem save(SampleItem item);

  Optional<SampleItem> findById(Long id);

  long nextId();
}
