package me.bchieu.messaging.modules.identity.api;

import jakarta.validation.Valid;
import me.bchieu.messaging.common.response.ApiResponse;
import me.bchieu.messaging.modules.identity.application.CreateUserCommand;
import me.bchieu.messaging.modules.identity.application.IdentityUserService;
import me.bchieu.messaging.modules.identity.domain.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** HTTP endpoints for identity user management. */
@RestController
@RequestMapping("/api/v1/identity/users")
public class IdentityUserController {
  private final IdentityUserService identityUserService;

  /** Creates the controller with the identity user service dependency. */
  public IdentityUserController(IdentityUserService identityUserService) {
    this.identityUserService = identityUserService;
  }

  /** Creates a new identity user. */
  @PostMapping
  public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    User createdUser =
        identityUserService.create(
            new CreateUserCommand(
                request.username(), request.displayName(), request.avatarUrl(), request.status()));
    return ApiResponse.success(UserResponse.from(createdUser));
  }

  /** Returns an identity user by username. */
  @GetMapping("/by-username/{username}")
  public ApiResponse<UserResponse> getByUsername(@PathVariable String username) {
    return ApiResponse.success(UserResponse.from(identityUserService.getByUsername(username)));
  }
}
