package org.oenexa.wallet.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void testGenerateTokenAndExtractUsername() {
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(userDetails);
        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void testValidateTokenWithDifferentUser() {
        String token = jwtUtil.generateToken(userDetails);
        UserDetails differentUser = new User("otheruser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        assertFalse(jwtUtil.validateToken(token, differentUser));
    }
}
