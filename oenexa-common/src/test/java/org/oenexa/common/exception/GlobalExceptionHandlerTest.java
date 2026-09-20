package org.oenexa.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.common.dto.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GlobalExceptionHandler BDD Test Suite")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private MockHttpServletRequest request;

    @SuppressWarnings("unused")
    private void sampleValidationTarget(String value) {
    }

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/v1/test");
        request.setRequestURI("/api/v1/test");
    }

    @Test
    @DisplayName("Given BusinessException thrown, When handleBusinessException is invoked, Then returns structured ErrorResponse with preserved HTTP status")
    void testHandleBusinessException() {
        // Given: BusinessException with CONFLICT status
        BusinessException ex = new BusinessException("Business rule violated", HttpStatus.CONFLICT);

        // When: exception handler processes exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);

        // Then: response matches status code and structured error payload
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Business rule violated", response.getBody().getMessage());
        assertEquals("/api/v1/test", response.getBody().getPath());
    }

    @Test
    @DisplayName("Given ResourceNotFoundException thrown, When handleResourceNotFoundException is invoked, Then returns HTTP 404 NOT_FOUND ErrorResponse")
    void testHandleResourceNotFoundException() {
        // Given: ResourceNotFoundException for missing Wallet
        ResourceNotFoundException ex = new ResourceNotFoundException("Wallet", "uuid", "123-456");

        // When: exception handler processes exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        // Then: HTTP 404 with formatted resource details returned
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Wallet"));
    }

    @Test
    @DisplayName("Given MethodArgumentNotValidException with binding errors, When handleValidationException is invoked, Then returns HTTP 400 with field errors")
    void testHandleValidationException() throws Exception {
        // Given: Native MethodArgumentNotValidException containing FieldError
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("sampleValidationTarget", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        FieldError fieldError = new FieldError("object", "amount", -5, false, null, null, "Amount must be positive");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        // When: exception handler processes validation exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        // Then: HTTP 400 Bad Request with field-level error messages returned
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals("amount", response.getBody().getErrors().get(0).getField());
        assertEquals("Amount must be positive", response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    @DisplayName("Given unhandled RuntimeException, When handleGenericException is invoked, Then returns HTTP 500 INTERNAL_SERVER_ERROR")
    void testHandleGenericException() {
        // Given: generic unhandled exception
        Exception ex = new RuntimeException("Unexpected runtime error");

        // When: generic exception handler invoked
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex, request);

        // Then: unified HTTP 500 error returned
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Given domain exception instantiations, When constructors are evaluated, Then properties and default status codes are preserved")
    void testCustomExceptions() {
        // Given & When: instantiating custom business exceptions
        BusinessException b1 = new BusinessException("Generic error");
        BusinessException b2 = new BusinessException("Custom error", HttpStatus.FORBIDDEN);
        InsufficientBalanceException ibe = new InsufficientBalanceException("Insufficient BTC balance: required 1.5, available 0.5");
        DuplicateResourceException dre = new DuplicateResourceException("User already exists with email test@oenexa.io");
        UnauthorizedException ue = new UnauthorizedException("Access Denied");

        // Then: status codes and messages are preserved accurately
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, b1.getStatus());
        assertEquals("Generic error", b1.getMessage());

        assertEquals(HttpStatus.FORBIDDEN, b2.getStatus());
        assertEquals("Custom error", b2.getMessage());

        assertEquals(HttpStatus.BAD_REQUEST, ibe.getStatus());
        assertEquals("Insufficient BTC balance: required 1.5, available 0.5", ibe.getMessage());

        assertEquals(HttpStatus.CONFLICT, dre.getStatus());
        assertEquals("User already exists with email test@oenexa.io", dre.getMessage());

        assertEquals(HttpStatus.UNAUTHORIZED, ue.getStatus());
        assertEquals("Access Denied", ue.getMessage());
    }
}
