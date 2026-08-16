package org.oenexa.identity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.identity.dto.request.LoginRequest;
import org.oenexa.identity.dto.request.RegisterRequest;
import org.oenexa.identity.dto.response.LoginResponse;
import org.oenexa.identity.dto.response.RegisterResponse;
import org.oenexa.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AuthController BDD Integration Test Suite")
public class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        httpClient = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Given new user details, When POST /api/v1/auth/register and /api/v1/auth/login are executed, Then returns 201 Created and 200 OK with valid JWT")
    void shouldRegisterAndLoginSuccessfully() throws Exception {
        // Given: valid user registration details
        RegisterRequest registerRequest = new RegisterRequest(
                "John", "Doe", "john.doe@example.com", "Password123!"
        );

        HttpRequest reqRegister = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerRequest)))
                .build();

        // When: register endpoint is called
        HttpResponse<String> resRegister = httpClient.send(reqRegister, HttpResponse.BodyHandlers.ofString());

        // Then: user created with HTTP 201 and non-blank UUID
        assertThat(resRegister.statusCode()).isEqualTo(201);
        RegisterResponse regResponse = objectMapper.readValue(resRegister.body(), RegisterResponse.class);
        assertThat(regResponse.uuid()).isNotBlank();

        // Given: registered credentials for login
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "Password123!");
        HttpRequest reqLogin = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                .build();

        // When: login endpoint is called
        HttpResponse<String> resLogin = httpClient.send(reqLogin, HttpResponse.BodyHandlers.ofString());

        // Then: HTTP 200 returned with valid access token
        assertThat(resLogin.statusCode()).isEqualTo(200);
        LoginResponse loginResponse = objectMapper.readValue(resLogin.body(), LoginResponse.class);
        assertThat(loginResponse.accessToken()).isNotBlank();
    }
}
