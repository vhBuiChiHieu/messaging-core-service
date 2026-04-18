package me.bchieu.base.modules.auth.api;

import jakarta.validation.Valid;
import me.bchieu.base.common.response.ApiResponse;
import me.bchieu.base.modules.auth.api.request.LoginRequest;
import me.bchieu.base.modules.auth.api.response.LoginResponse;
import me.bchieu.base.modules.auth.application.mapper.AuthMapper;
import me.bchieu.base.modules.auth.application.service.AuthApplicationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthApplicationService authApplicationService;

  public AuthController(AuthApplicationService authApplicationService) {
    this.authApplicationService = authApplicationService;
  }

  @PostMapping("/login")
  public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    String token =
        authApplicationService.login(
            AuthMapper.toLoginCommand(request.username(), request.password()));
    return ApiResponse.success(new LoginResponse(token));
  }
}
