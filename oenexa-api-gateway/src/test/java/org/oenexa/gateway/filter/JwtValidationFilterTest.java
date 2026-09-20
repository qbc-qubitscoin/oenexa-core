package org.oenexa.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.security.jwt.JwtProperties;
import org.oenexa.security.jwt.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class JwtValidationFilterTest {

    static class StubJwtTokenProvider extends JwtTokenProvider {
        boolean valid = true;
        String userId = "user-42";

        public StubJwtTokenProvider() {
            super(new JwtProperties());
        }

        @Override
        public boolean validateToken(String token) {
            return "valid-token".equals(token) && valid;
        }

        @Override
        public String getUserIdFromToken(String token) {
            return userId;
        }
    }

    static class RecordingGatewayFilterChain implements GatewayFilterChain {
        int filterCallCount = 0;
        ServerWebExchange recordedExchange;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.filterCallCount++;
            this.recordedExchange = exchange;
            return Mono.empty();
        }
    }

    private StubJwtTokenProvider jwtTokenProvider;
    private JwtValidationFilter filter;
    private RecordingGatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new StubJwtTokenProvider();
        filter = new JwtValidationFilter(jwtTokenProvider);
        chain = new RecordingGatewayFilterChain();
    }

    @Test
    @DisplayName("Given filter instance, When getOrder is queried, Then return precedence -1")
    void testGetOrder() {
        // Given - JwtValidationFilter instance

        // When & Then
        assertEquals(-1, filter.getOrder());
    }

    @Test
    @DisplayName("Given public endpoint request, When filter processes request, Then bypass authentication and continue chain")
    void testFilter_BypassesAuth() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(1, chain.filterCallCount);
        assertSame(exchange, chain.recordedExchange);
    }

    @Test
    @DisplayName("Given request without Authorization header, When filter processes request, Then respond with 401 Unauthorized")
    void testFilter_NoAuthHeader() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/wallets").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(0, chain.filterCallCount);
    }

    @Test
    @DisplayName("Given request with non-Bearer Authorization header, When filter processes request, Then respond with 401 Unauthorized")
    void testFilter_InvalidAuthHeaderPrefix() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/wallets")
                .header(HttpHeaders.AUTHORIZATION, "Basic dGVzdDp0ZXN0")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(0, chain.filterCallCount);
    }

    @Test
    @DisplayName("Given request with invalid JWT token, When filter processes request, Then respond with 401 Unauthorized")
    void testFilter_InvalidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/wallets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        jwtTokenProvider.valid = false;

        // When
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(0, chain.filterCallCount);
    }

    @Test
    @DisplayName("Given request with valid JWT token, When filter processes request, Then forward request with X-User-Id header")
    void testFilter_ValidToken() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/wallets")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        jwtTokenProvider.valid = true;
        jwtTokenProvider.userId = "user-42";

        // When
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(1, chain.filterCallCount);
        assertNotNull(chain.recordedExchange);
        assertEquals("user-42", chain.recordedExchange.getRequest().getHeaders().getFirst("X-User-Id"));
    }
}
