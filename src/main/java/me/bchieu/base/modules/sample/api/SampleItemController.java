package me.bchieu.base.modules.sample.api;

import jakarta.validation.Valid;
import me.bchieu.base.common.response.ApiResponse;
import me.bchieu.base.modules.sample.api.request.CreateSampleItemRequest;
import me.bchieu.base.modules.sample.api.response.SampleItemResponse;
import me.bchieu.base.modules.sample.application.dto.CreateSampleItemCommand;
import me.bchieu.base.modules.sample.application.service.SampleItemApplicationService;
import me.bchieu.base.modules.sample.domain.model.SampleItem;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample-items")
public class SampleItemController {

  private final SampleItemApplicationService sampleItemApplicationService;

  public SampleItemController(SampleItemApplicationService sampleItemApplicationService) {
    this.sampleItemApplicationService = sampleItemApplicationService;
  }

  @PostMapping
  public ApiResponse<SampleItemResponse> create(
      @Valid @RequestBody CreateSampleItemRequest request) {
    SampleItem item =
        sampleItemApplicationService.create(new CreateSampleItemCommand(request.name()));
    return ApiResponse.success(new SampleItemResponse(item.id(), item.name(), item.createdAt()));
  }
}
