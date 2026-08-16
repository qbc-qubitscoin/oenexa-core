package org.oenexa.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.wallet.dto.AuthRequest;
import org.oenexa.wallet.dto.AuthResponse;
import org.oenexa.wallet.dto.RegisterRequest;
import org.oenexa.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AuthController BDD Test Suite")
class AuthControllerTest {

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid RegisterRequest, When POST /api/v1/auth/register is invoked, Then returns HTTP 200 with AuthResponse")
    void testRegister() throws Exception {
        // Given: registration request
        RegisterRequest request = new RegisterRequest("controller@test.com", "password");

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .build();

        // When: request is sent
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // Then: HTTP 200 with valid token returned
        assertEquals(200, response.statusCode());
        AuthResponse authResponse = objectMapper.readValue(response.body(), AuthResponse.class);
        assertEquals("controller@test.com", authResponse.getEmail());
        assertNotNull(authResponse.getToken());
    }

    @Test
    @DisplayName("Given existing user credentials, When POST /api/v1/auth/login is invoked, Then returns HTTP 200 with JWT token")
    void testLogin() throws Exception {
        // Given: registered user
        RegisterRequest registerRequest = new RegisterRequest("login-controller@test.com", "password");
        HttpRequest regReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerRequest)))
                .build();
        httpClient.send(regReq, HttpResponse.BodyHandlers.ofString());

        // When: login requested
        AuthRequest loginRequest = new AuthRequest("login-controller@test.com", "password");
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(loginRequest)))
                .build();

        HttpResponse<String> loginResp = httpClient.send(loginReq, HttpResponse.BodyHandlers.ofString());

        // Then: HTTP 200 returned with valid auth token
        assertEquals(200, loginResp.statusCode());
        AuthResponse authResponse = objectMapper.readValue(loginResp.body(), AuthResponse.class);
        assertEquals("login-controller@test.com", authResponse.getEmail());
        assertNotNull(authResponse.getToken());
    }
}
