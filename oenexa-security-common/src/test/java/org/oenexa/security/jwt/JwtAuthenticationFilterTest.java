package org.oenexa.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.security.config.SecurityConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtAuthenticationFilter BDD Test Suite")
class JwtAuthenticationFilterTest {

    private StubJwtTokenProvider tokenProvider;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private CountFilterChain filterChain;

    static class StubJwtTokenProvider extends JwtTokenProvider {
        boolean valid = true;
        boolean shouldThrow = false;
        String userId = "trader@oenexa.io";

        public StubJwtTokenProvider() {
            super(new JwtProperties());
        }

        @Override
        public boolean validateToken(String token) {
            if (shouldThrow) {
                throw new RuntimeException("Token parsing error");
            }
            return valid;
        }

        @Override
        public String getUserIdFromToken(String token) {
            return userId;
        }
    }

    static class CountFilterChain implements FilterChain {
        int callCount = 0;

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
            callCount++;
        }
    }

    @BeforeEach
    void setUp() {
        tokenProvider = new StubJwtTokenProvider();
        userDetailsService = username -> new User(username, "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        filter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new CountFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Given valid Bearer JWT header, When doFilterInternal is executed, Then authentication context is populated and chain proceeds")
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        // Given: request with valid Bearer token and resolved user
        String token = "valid.jwt.token";
        String email = "trader@oenexa.io";
        tokenProvider.valid = true;
        tokenProvider.userId = email;
        request.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

        // When: filter executes
        filter.doFilterInternal(request, response, filterChain);

        // Then: authentication context established and chain continued
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(email, ((UserDetails) auth.getPrincipal()).getUsername());
        assertEquals(1, filterChain.callCount);
    }

    @Test
    @DisplayName("Given invalid Bearer JWT header, When doFilterInternal is executed, Then authentication context is skipped and chain proceeds")
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        // Given: request with invalid token
        String token = "invalid.jwt.token";
        tokenProvider.valid = false;
        request.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

        // When: filter executes
        filter.doFilterInternal(request, response, filterChain);

        // Then: authentication is null and filter chain proceeds
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1, filterChain.callCount);
    }

    @Test
    @DisplayName("Given missing or non-Bearer authorization header, When doFilterInternal is executed, Then authentication is bypassed and chain proceeds")
    void testDoFilterInternal_NoBearerHeader() throws ServletException, IOException {
        // Given: null header
        // When: filter executes
        filter.doFilterInternal(request, response, filterChain);

        // Then: authentication remains null
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1, filterChain.callCount);

        // Given: Basic auth header instead of Bearer
        request.addHeader(SecurityConstants.HEADER_STRING, "Basic dXNlcjpwYXNz");

        // When: filter executes again
        filter.doFilterInternal(request, response, filterChain);

        // Then: chain executed second time
        assertEquals(2, filterChain.callCount);
    }

    @Test
    @DisplayName("Given token validation throws Exception, When doFilterInternal is executed, Then error is caught and chain continues")
    void testDoFilterInternal_ExceptionHandled() throws ServletException, IOException {
        // Given: token provider throws runtime error
        String token = "throwing.jwt.token";
        tokenProvider.shouldThrow = true;
        request.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + token);

        // When: filter executes
        filter.doFilterInternal(request, response, filterChain);

        // Then: security context is empty and filter chain continues safely
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1, filterChain.callCount);
    }
}
