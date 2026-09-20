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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AuthServiceImplIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("identity_db")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.flyway.url", mysql::getJdbcUrl);
        registry.add("spring.flyway.user", mysql::getUsername);
        registry.add("spring.flyway.password", mysql::getPassword);
    }

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
