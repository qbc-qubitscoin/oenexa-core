package org.oenexa.identity.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oenexa.identity.entity.UserEntity;
import org.oenexa.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CustomUserDetailsServiceIntegrationTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldLoadUserByUsername() {
        UserEntity user = new UserEntity();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail("custom.user@example.com");
        user.setPasswordHash("hashed_pass");
        user.setFirstName("Custom");
        user.setLastName("User");
        user.setRoles("ROLE_USER, ,ROLE_ADMIN");
        user.setAccountStatus("ACTIVE");
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername("custom.user@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("custom.user@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashed_pass");
        assertThat(userDetails.getAuthorities()).hasSize(2);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("non.existent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: non.existent@example.com");
    }
}
