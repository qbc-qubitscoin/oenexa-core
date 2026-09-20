package org.oenexa.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.common.constant.AccountStatus;
import org.oenexa.security.model.UserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtTokenProvider BDD Test Suite")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private JwtProperties jwtProperties;
    private UserPrincipal testUser;

    @BeforeEach
    void setUp() {
        // Given: initialized JWT properties and test user principal
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("super-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-256!");
        jwtProperties.setIssuer("oenexa-test");
        jwtProperties.setAccessTokenExpiration(60000); // 1 min
        jwtProperties.setRefreshTokenExpiration(120000); // 2 mins

        tokenProvider = new JwtTokenProvider(jwtProperties);

        testUser = UserPrincipal.builder()
                .id(100L)
                .uuid(UUID.randomUUID().toString())
                .email("trader@oenexa.io")
                .password("encoded-secret")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .enabled(true)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Given valid UserPrincipal, When generateAccessToken is called, Then valid signed JWT containing user identity is produced")
    void testGenerateAccessToken() {
        // When: access token is generated
        String token = tokenProvider.generateAccessToken(testUser);

        // Then: token is valid and parses user identity correctly
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("trader@oenexa.io", tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Given valid UserPrincipal, When generateRefreshToken is called, Then valid signed refresh JWT is produced")
    void testGenerateRefreshToken() {
        // When: refresh token is generated
        String token = tokenProvider.generateRefreshToken(testUser);

        // Then: refresh token validates and preserves identity
        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("trader@oenexa.io", tokenProvider.getUserIdFromToken(token));
    }

    @Test
    @DisplayName("Given invalid or tampered JWT string, When validateToken is evaluated, Then false is returned")
    void testValidateToken_Invalid() {
        // Given: null, empty, or garbage token strings
        // When & Then: validation fails
        assertFalse(tokenProvider.validateToken(null));
        assertFalse(tokenProvider.validateToken(""));
        assertFalse(tokenProvider.validateToken("invalid.jwt.token"));

        // Given: valid token that was tampered
        String token = tokenProvider.generateAccessToken(testUser);
        String tamperedToken = token + "corrupted";

        // When & Then: validation detects tampering and returns false
        assertFalse(tokenProvider.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Given expired JWT token, When validateToken is evaluated, Then false is returned")
    void testValidateToken_Expired() {
        // Given: token generated with negative expiration
        jwtProperties.setAccessTokenExpiration(-1000);
        String expiredToken = tokenProvider.generateAccessToken(testUser);

        // When & Then: validation detects expiration and returns false
        assertFalse(tokenProvider.validateToken(expiredToken));
    }
}
