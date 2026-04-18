package me.bchieu.base.modules.sample.api.response;

import java.time.Instant;

public record SampleItemResponse(Long id, String name, Instant createdAt) {}
