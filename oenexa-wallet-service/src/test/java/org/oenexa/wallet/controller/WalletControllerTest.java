package org.oenexa.wallet.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.wallet.dto.AuthResponse;
import org.oenexa.wallet.dto.RegisterRequest;
import org.oenexa.wallet.entity.User;
import org.oenexa.wallet.entity.WalletEntity;
import org.oenexa.wallet.repository.UserRepository;
import org.oenexa.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("WalletController BDD Test Suite")
class WalletControllerTest {

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    private HttpClient httpClient = HttpClient.newHttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Given authenticated user with wallet balances, When GET /api/v1/wallets is invoked, Then returns HTTP 200 with wallet list")
    void testGetWallets() throws Exception {
        // Given: registered user with JWT token
        RegisterRequest registerRequest = new RegisterRequest("wallet-user@test.com", "password");

        HttpRequest authReq = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerRequest)))
                .build();

        HttpResponse<String> authResp = httpClient.send(authReq, HttpResponse.BodyHandlers.ofString());
        AuthResponse authResponse = objectMapper.readValue(authResp.body(), AuthResponse.class);

        User user = userRepository.findByEmail("wallet-user@test.com").orElseThrow();
        walletService.addBalance(user.getId(), "USD", BigDecimal.valueOf(250));

        // When: authenticated GET /api/v1/wallets/{userId} is executed
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + user.getId()))
                .header("Authorization", "Bearer " + authResponse.getToken())
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // Then: HTTP 200 returned with user wallet details
        assertEquals(200, resp.statusCode());
        List<WalletEntity> wallets = objectMapper.readValue(resp.body(), new TypeReference<List<WalletEntity>>() {});
        assertFalse(wallets.isEmpty());
        assertEquals("USD", wallets.get(0).getCurrency());
        assertEquals(0, BigDecimal.valueOf(250).compareTo(wallets.get(0).getBalance()));
    }

    @Test
    @DisplayName("Given request without Bearer token, When accessing /api/v1/wallets/{userId}, Then returns HTTP 403 Forbidden")
    void testGetWallets_Unauthorized() throws Exception {
        // Given: unauthenticated request
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/1"))
                .GET()
                .build();

        // When: request sent
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        // Then: HTTP 403 Forbidden returned
        assertEquals(403, resp.statusCode());
    }
}
