package me.bchieu.messaging.common.exception;

import java.util.stream.Collectors;
import me.bchieu.messaging.common.response.ApiResponse;
import me.bchieu.messaging.modules.identity.domain.IdentityUserAlreadyExistsException;
import me.bchieu.messaging.modules.identity.domain.IdentityUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps application exceptions to standard API responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** Returns a bad-request response for bean validation failures. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    // Gom toàn bộ lỗi validate để dễ theo dõi từ phía client.
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining("; "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(false, message, null));
  }

  /** Returns a conflict response for duplicated usernames. */
  @ExceptionHandler(IdentityUserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserAlreadyExists(
      IdentityUserAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }

  /** Returns a not-found response for missing identity users. */
  @ExceptionHandler(IdentityUserNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleIdentityUserNotFound(
      IdentityUserNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, exception.getMessage(), null));
  }
}
