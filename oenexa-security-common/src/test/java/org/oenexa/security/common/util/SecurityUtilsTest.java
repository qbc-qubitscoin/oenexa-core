package org.oenexa.security.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityUtils BDD Test Suite")
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Given authenticated security context with UUID principal, When getCurrentUserId is called, Then parsed UUID is returned")
    void testGetCurrentUserId_Success() {
        // Given: security context with authenticated valid UUID
        UUID expectedUuid = UUID.randomUUID();
        Authentication auth = new UsernamePasswordAuthenticationToken(expectedUuid.toString(), "n/a", Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // When: user ID extracted
        UUID actual = SecurityUtils.getCurrentUserId();

        // Then: matches expected UUID
        assertEquals(expectedUuid, actual);
    }

    @Test
    @DisplayName("Given null authentication in security context, When getCurrentUserId is called, Then RuntimeException is thrown")
    void testGetCurrentUserId_Unauthenticated() {
        // Given: null authentication in security context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(null);
        SecurityContextHolder.setContext(context);

        // When & Then: RuntimeException thrown
        RuntimeException ex = assertThrows(RuntimeException.class, SecurityUtils::getCurrentUserId);
        assertEquals("User not authenticated or invalid token", ex.getMessage());
    }

    @Test
    @DisplayName("Given authentication with null name, When getCurrentUserId is called, Then RuntimeException is thrown")
    void testGetCurrentUserId_NullName() {
        // Given: native stub authentication with null name
        Authentication authWithNullName = new Authentication() {
            @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return null; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
            @Override public String getName() { return null; }
        };

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authWithNullName);
        SecurityContextHolder.setContext(context);

        // When & Then: RuntimeException thrown
        RuntimeException ex = assertThrows(RuntimeException.class, SecurityUtils::getCurrentUserId);
        assertEquals("User not authenticated or invalid token", ex.getMessage());
    }

    @Test
    @DisplayName("Given anonymousUser principal in security context, When getCurrentUserId is called, Then RuntimeException is thrown")
    void testGetCurrentUserId_AnonymousUser() {
        // Given: anonymousUser authentication
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", "n/a", Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // When & Then: RuntimeException thrown
        RuntimeException ex = assertThrows(RuntimeException.class, SecurityUtils::getCurrentUserId);
        assertEquals("User not authenticated or invalid token", ex.getMessage());
    }

    @Test
    @DisplayName("Given principal name not formatted as UUID, When getCurrentUserId is called, Then wrapped RuntimeException is thrown")
    void testGetCurrentUserId_InvalidUuid() {
        // Given: non-UUID principal name
        Authentication auth = new UsernamePasswordAuthenticationToken("not-a-valid-uuid", "n/a", Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // When & Then: RuntimeException wrapping IllegalArgumentException thrown
        RuntimeException ex = assertThrows(RuntimeException.class, SecurityUtils::getCurrentUserId);
        assertTrue(ex.getMessage().contains("User ID in token is not a valid UUID"));
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
    }

    @Test
    @DisplayName("Given SecurityUtils private constructor, When invoked via reflection, Then utility instance is instantiated")
    void testPrivateConstructor() throws Exception {
        // Given: private constructor
        Constructor<SecurityUtils> constructor = SecurityUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // When: instantiated
        SecurityUtils instance = constructor.newInstance();

        // Then: non-null instance
        assertNotNull(instance);
    }
}
