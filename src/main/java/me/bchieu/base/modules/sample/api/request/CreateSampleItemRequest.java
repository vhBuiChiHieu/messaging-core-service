package me.bchieu.base.modules.sample.api.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSampleItemRequest(@NotBlank String name) {}
