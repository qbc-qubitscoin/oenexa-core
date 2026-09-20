package org.oenexa.identity.service.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oenexa.identity.dto.request.RegisterRequest;
import org.oenexa.identity.dto.response.RegisterResponse;
import org.oenexa.identity.repository.UserRepository;
import org.oenexa.identity.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceImplIntegrationTest {


    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("Test", "User", "test.user@example.com", "Password123!");

        // When
        RegisterResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.uuid()).isNotBlank();
        assertThat(userRepository.existsByEmail("test.user@example.com")).isTrue();
    }

    @Test
    void shouldVerifyEmailSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("Test2", "User2", "test2.user@example.com", "Password123!");
        authService.register(request);
        
        // When
        authService.verifyEmail("test2.user@example.com", "123456");

        // Then
        var user = userRepository.findByEmail("test2.user@example.com").orElseThrow();
        assertThat(user.getEmailVerified()).isTrue();
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest("Test3", "User3", "test3.user@example.com", "Password123!");
        authService.register(request);

        // When
        var loginResponse = authService.login(new org.oenexa.identity.dto.request.LoginRequest("test3.user@example.com", "Password123!"));

        // Then
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessToken()).isNotBlank();
        assertThat(loginResponse.refreshToken()).isNotBlank();
    }
}
