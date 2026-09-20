package org.oenexa.user.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.common.dto.ErrorResponse;
import org.oenexa.user.dto.request.UpdateUserProfileRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD/TDD unit tests for {@link GlobalUserExceptionHandler}.
 *
 * <p>Uses {@link MockHttpServletRequest} (Spring native) for request simulation.
 * Uses {@link BeanPropertyBindingResult} (Spring native) as the concrete {@link BindingResult}
 * implementation — zero Mockito, zero hand-rolled stubs.
 */
@DisplayName("GlobalUserExceptionHandler BDD Test Suite")
class GlobalUserExceptionHandlerTest {

    private GlobalUserExceptionHandler handler;
    private MockHttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalUserExceptionHandler();
        mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/users/me");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleUserProfileNotFound
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given UserProfileNotFoundException, When handleUserProfileNotFound is called, Then HTTP 404 with structured error body is returned")
    void testHandleUserProfileNotFound_Returns404WithErrorBody() {
        // Given: a typed not-found exception for a specific user
        UUID userId = UUID.randomUUID();
        UserProfileNotFoundException ex = new UserProfileNotFoundException(userId);

        // When: the exception handler processes the exception
        ResponseEntity<ErrorResponse> response = handler.handleUserProfileNotFound(ex, mockRequest);

        // Then: response is HTTP 404 with complete structured error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains(userId.toString());
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Given UserProfileNotFoundException, When handleUserProfileNotFound is called, Then response body contains no validation errors")
    void testHandleUserProfileNotFound_ResponseBodyHasNoValidationErrors() {
        // Given
        UUID userId = UUID.randomUUID();
        UserProfileNotFoundException ex = new UserProfileNotFoundException(userId);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleUserProfileNotFound(ex, mockRequest);

        // Then: no field-level errors — this is not a validation response
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).isNull();
    }

    @Test
    @DisplayName("Given two different UserProfileNotFoundExceptions, When handled, Then each response contains its own userId in the message")
    void testHandleUserProfileNotFound_MessageContainsCorrectUserId() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        // When
        ResponseEntity<ErrorResponse> response1 = handler.handleUserProfileNotFound(
                new UserProfileNotFoundException(userId1), mockRequest);
        ResponseEntity<ErrorResponse> response2 = handler.handleUserProfileNotFound(
                new UserProfileNotFoundException(userId2), mockRequest);

        // Then: each response message contains its own UUID
        assertThat(response1.getBody()).isNotNull();
        assertThat(response1.getBody().getMessage()).contains(userId1.toString());
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getMessage()).contains(userId2.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleValidation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given MethodArgumentNotValidException with two field errors, When handleValidation is called, Then HTTP 400 with per-field errors is returned")
    void testHandleValidation_WithFieldErrors_Returns400WithErrors() {
        // Given: a BeanPropertyBindingResult with two injected field errors (Spring native)
        UpdateUserProfileRequest target = new UpdateUserProfileRequest(
                null, null, null, null, null, null, null, null);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "updateUserProfileRequest");
        bindingResult.rejectValue("firstName", "NotBlank", "First name is required");
        bindingResult.rejectValue("country", "NotBlank", "Country is required");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, mockRequest);

        // Then: 400 with per-field validation error list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors()).hasSize(2);
        assertThat(response.getBody().getErrors())
                .extracting("field")
                .containsExactlyInAnyOrder("firstName", "country");
        assertThat(response.getBody().getErrors())
                .extracting("message")
                .containsExactlyInAnyOrder("First name is required", "Country is required");
    }

    @Test
    @DisplayName("Given MethodArgumentNotValidException with no field errors, When handleValidation is called, Then HTTP 400 with empty errors list is returned")
    void testHandleValidation_WithNoFieldErrors_Returns400WithEmptyList() {
        // Given: binding result with no errors
        UpdateUserProfileRequest target = new UpdateUserProfileRequest(
                "Valid", "User", java.time.LocalDate.of(1990, 1, 1),
                "123 St", null, "City", "UK", "SW1A");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "updateUserProfileRequest");

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, mockRequest);

        // Then: 400 with empty errors list
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrors()).isEmpty();
    }

    @Test
    @DisplayName("Given MethodArgumentNotValidException, When handleValidation is called, Then path in error body matches request URI")
    void testHandleValidation_ResponsePathMatchesRequestUri() {
        // Given
        mockRequest.setRequestURI("/api/v1/users/me");
        UpdateUserProfileRequest target = new UpdateUserProfileRequest(
                null, null, null, null, null, null, null, null);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.rejectValue("firstName", "NotBlank", "Required");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, mockRequest);

        // Then: path is captured from request
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/me");
    }
}
