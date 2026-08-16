package org.oenexa.wallet.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.wallet.entity.Role;
import org.oenexa.wallet.entity.User;
import org.oenexa.wallet.repository.RoleRepository;
import org.oenexa.wallet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserDetailsServiceImpl.class)
@DisplayName("UserDetailsServiceImpl BDD Test Suite")
class UserDetailsServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Given persisted user with roles, When loadUserByUsername is called, Then UserDetails with authorities is returned")
    void testLoadUserByUsername_Success() {
        // Given: persisted user with ROLE_USER
        Role role = new Role(null, "ROLE_USER");
        role = roleRepository.save(role);

        User user = new User();
        user.setEmail("testuser@test.com");
        user.setPassword("hashedpassword");
        user.setRoles(Set.of(role));
        userRepository.save(user);

        // When: user loaded by email
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser@test.com");

        // Then: correct username, password, and authorities returned
        assertNotNull(userDetails);
        assertEquals("testuser@test.com", userDetails.getUsername());
        assertEquals("hashedpassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Given non-existent user email, When loadUserByUsername is called, Then UsernameNotFoundException is thrown")
    void testLoadUserByUsername_NotFound() {
        // Given: unregistered email
        // When & Then: UsernameNotFoundException thrown
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("unknown@test.com");
        });
    }
}
