package me.bchieu.base.modules.sample.domain.model;

import java.time.Instant;

public record SampleItem(Long id, String name, Instant createdAt) {}
