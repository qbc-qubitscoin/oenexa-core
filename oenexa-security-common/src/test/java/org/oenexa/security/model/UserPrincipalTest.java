package org.oenexa.security.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.common.constant.AccountStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserPrincipal BDD Test Suite")
class UserPrincipalTest {

    @Test
    @DisplayName("Given UserPrincipal model, When instantiated and account statuses change, Then security invariants are verified")
    void testUserPrincipal() {
        // Given: initialized UserPrincipal builder
        SimpleGrantedAuthority auth = new SimpleGrantedAuthority("ROLE_ADMIN");
        UserPrincipal user = UserPrincipal.builder()
                .id(42L)
                .uuid("test-uuid-42")
                .email("admin@oenexa.io")
                .password("secret42")
                .authorities(Collections.singletonList(auth))
                .enabled(true)
                .status(AccountStatus.ACTIVE)
                .build();

        // When & Then: assert getters and active status checks
        assertEquals(42L, user.getId());
        assertEquals("test-uuid-42", user.getUuid());
        assertEquals("admin@oenexa.io", user.getEmail());
        assertEquals("admin@oenexa.io", user.getUsername());
        assertEquals("secret42", user.getPassword());
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.isEnabled());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isAccountNonLocked());

        // When: status set to LOCKED
        user.setStatus(AccountStatus.LOCKED);
        // Then: account is not non-locked
        assertFalse(user.isAccountNonLocked());

        // When: status set to CLOSED
        user.setStatus(AccountStatus.CLOSED);
        // Then: account is locked/closed
        assertFalse(user.isAccountNonLocked());

        // When: status set to SUSPENDED
        user.setStatus(AccountStatus.SUSPENDED);
        // Then: account non-locked returns true
        assertTrue(user.isAccountNonLocked());

        // When: enabled set to false
        user.setEnabled(false);
        // Then: isEnabled returns false
        assertFalse(user.isEnabled());

        // Given: no-arg constructor & setters
        UserPrincipal user2 = new UserPrincipal();
        user2.setId(1L);
        user2.setUuid("uuid1");
        user2.setEmail("u1@oenexa.io");
        user2.setPassword("pwd");
        user2.setAuthorities(Collections.emptyList());
        user2.setEnabled(true);
        user2.setStatus(AccountStatus.ACTIVE);

        // When & Then: verify properties and equality
        assertEquals(1L, user2.getId());
        assertEquals("uuid1", user2.getUuid());
        assertNotNull(user2.toString());
        assertEquals(user2, user2);
        assertNotEquals(user, user2);
    }

    @Test
    @DisplayName("Given AuthenticatedUser record, When instantiated, Then all record components are accessible")
    void testAuthenticatedUser() {
        // Given: AuthenticatedUser record parameters
        // When: constructed
        AuthenticatedUser authUser = new AuthenticatedUser(1L, "u-1", "user@oenexa.io", List.of("ROLE_USER"));

        // Then: record components are correctly stored
        assertEquals(1L, authUser.userId());
        assertEquals("u-1", authUser.uuid());
        assertEquals("user@oenexa.io", authUser.email());
        assertEquals(List.of("ROLE_USER"), authUser.roles());
        assertNotNull(authUser.toString());
    }
}
