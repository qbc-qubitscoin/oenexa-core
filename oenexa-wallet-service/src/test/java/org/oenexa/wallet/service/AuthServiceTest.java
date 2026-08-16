package org.oenexa.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.wallet.dto.AuthRequest;
import org.oenexa.wallet.dto.AuthResponse;
import org.oenexa.wallet.dto.RegisterRequest;
import org.oenexa.wallet.entity.User;
import org.oenexa.wallet.repository.UserRepository;
import org.oenexa.wallet.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthService BDD Test Suite")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Given valid registration request, When register is invoked, Then user is persisted and valid JWT is issued")
    void testRegister_Success() {
        // Given: valid registration request
        RegisterRequest request = new RegisterRequest("test@test.com", "password123");

        // When: register is called
        AuthResponse response = authService.register(request);

        // Then: valid AuthResponse returned and user stored
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test@test.com", response.getEmail());

        User user = userRepository.findByEmail("test@test.com").orElseThrow();
        assertEquals("test@test.com", user.getEmail());
        assertTrue(jwtUtil.validateToken(response.getToken(), user));
    }

    @Test
    @DisplayName("Given existing user email, When registering again with same email, Then RuntimeException is thrown")
    void testRegister_EmailAlreadyExists() {
        // Given: previously registered user
        RegisterRequest request1 = new RegisterRequest("test@test.com", "password123");
        authService.register(request1);

        RegisterRequest request2 = new RegisterRequest("test@test.com", "anotherpassword");

        // When & Then: duplicate registration throws RuntimeException
        assertThrows(RuntimeException.class, () -> authService.register(request2));
    }

    @Test
    @DisplayName("Given registered user credentials, When login is executed, Then authentication succeeds and JWT token is issued")
    void testLogin_Success() {
        // Given: registered user
        RegisterRequest registerRequest = new RegisterRequest("login@test.com", "password123");
        authService.register(registerRequest);

        AuthRequest loginRequest = new AuthRequest("login@test.com", "password123");

        // When: user logs in
        AuthResponse response = authService.login(loginRequest);

        // Then: authentication token returned
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("login@test.com", response.getEmail());
    }

    @Test
    @DisplayName("Given unregistered user email, When login is executed, Then BadCredentialsException is thrown")
    void testLogin_UserNotFound() {
        // Given: non-existent user credentials
        AuthRequest loginRequest = new AuthRequest("unknown@test.com", "password123");

        // When & Then: login throws BadCredentialsException
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Given incorrect password for existing user, When login is executed, Then BadCredentialsException is thrown")
    void testLogin_WrongPassword() {
        // Given: registered user
        RegisterRequest registerRequest = new RegisterRequest("login@test.com", "password123");
        authService.register(registerRequest);

        AuthRequest loginRequest = new AuthRequest("login@test.com", "wrongpassword");

        // When & Then: login throws BadCredentialsException
        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
