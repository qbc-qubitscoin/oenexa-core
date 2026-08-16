package org.oenexa.wallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil BDD Test Suite")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000);

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new User("testuser", "password", authorities);
    }

    @Test
    @DisplayName("Given valid UserDetails, When generateToken and extractUsername are called, Then token is created and username matches")
    void testGenerateTokenAndExtractUsername() {
        // When: token is generated
        String token = jwtUtil.generateToken(userDetails);

        // Then: token is non-null and subject username matches
        assertNotNull(token);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    @DisplayName("Given valid token and matching user, When validateToken is called, Then returns true")
    void testValidateToken() {
        // Given: valid token
        String token = jwtUtil.generateToken(userDetails);

        // When & Then: validation succeeds
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    @DisplayName("Given token generated for different user, When validateToken is evaluated, Then returns false")
    void testValidateTokenWithDifferentUser() {
        // Given: valid token for testuser
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User("otheruser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // When & Then: validation with different user returns false
        assertFalse(jwtUtil.validateToken(token, differentUser));
    }

    @Test
    @DisplayName("Given expired token, When validateToken is evaluated, Then returns false")
    void testValidateToken_Expired() {
        // Given: token configured with negative expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000);
        String expiredToken = jwtUtil.generateToken(userDetails);

        // When & Then: validation returns false
        assertFalse(jwtUtil.validateToken(expiredToken, userDetails));
    }
}
