package org.oenexa.user.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.oenexa.common.dto.ErrorResponse;
import org.oenexa.common.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Global exception handler for the User Service.
 *
 * <p>Maps domain exceptions to structured HTTP error responses using {@link ErrorResponse}.
 */
@RestControllerAdvice
public class GlobalUserExceptionHandler {

    /**
     * Handles {@link UserProfileNotFoundException} thrown by service layer.
     *
     * @param ex      the exception carrying the missing user ID
     * @param request the current HTTP request for path extraction
     * @return HTTP 404 with structured {@link ErrorResponse}
     */
    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserProfileNotFound(
            UserProfileNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handles bean validation failures from {@code @Validated} controller parameters.
     *
     * @param ex      the validation exception containing field-level errors
     * @param request the current HTTP request for path extraction
     * @return HTTP 400 with per-field {@link ValidationError} list
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ValidationError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
