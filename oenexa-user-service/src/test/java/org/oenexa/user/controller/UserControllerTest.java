package org.oenexa.user.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.user.UserServiceApplication;
import org.oenexa.user.config.TestConfig;
import org.oenexa.user.dto.request.UpdatePreferencesRequest;
import org.oenexa.user.dto.request.UpdateUserProfileRequest;
import org.oenexa.user.dto.response.UserProfileDto;
import org.oenexa.user.entity.UserProfileEntity;
import org.oenexa.user.exception.UserProfileNotFoundException;
import org.oenexa.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BDD/TDD controller-level tests for {@link UserController}.
 *
 * <p>Tests invoke controller methods directly (no HTTP layer / MockMvc),
 * using real service and H2 repository. Security context is set up using
 * Spring's native {@link SecurityContextHolder}. Zero Mockito.
 */
@SpringBootTest(classes = {UserServiceApplication.class, TestConfig.class})
@ActiveProfiles("test")
@DisplayName("UserController BDD Test Suite")
class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();

        testUserId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(testUserId);
        entity.setFirstName("John");
        entity.setLastName("Doe");
        userProfileRepository.save(entity);

        // Given: an authenticated user in the security context
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUserId.toString(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        userProfileRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/users/me
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given authenticated user context with existing profile, When GET /api/v1/users/me is called, Then returns HTTP 200 with profile")
    void testGetMyProfile_WhenProfileExists_ThenReturns200() {
        // When: getMyProfile is called
        ResponseEntity<UserProfileDto> response = userController.getMyProfile();

        // Then: returns 200 with authenticated user profile
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userId()).isEqualTo(testUserId);
        assertThat(response.getBody().firstName()).isEqualTo("John");
        assertThat(response.getBody().lastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Given authenticated user context with no profile, When GET /api/v1/users/me is called, Then UserProfileNotFoundException is thrown")
    void testGetMyProfile_WhenProfileMissing_ThenThrowsNotFoundException() {
        // Given: the profile is deleted but the user is still authenticated
        userProfileRepository.deleteAll();

        // When & Then: typed 404 exception propagates
        assertThatThrownBy(() -> userController.getMyProfile())
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/v1/users/me
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given valid UpdateUserProfileRequest, When PUT /api/v1/users/me is called, Then returns HTTP 200 with updated profile")
    void testUpdateMyProfile_WhenValidRequest_ThenReturns200WithUpdatedProfile() {
        // Given: update request with new first name
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Johnny", "Doe", LocalDate.of(1995, 1, 1),
                "456 Avenue", "Suite 1", "City", "Country", "10001"
        );

        // When: profile is updated
        ResponseEntity<UserProfileDto> response = userController.updateMyProfile(request);

        // Then: 200 OK with updated name
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().firstName()).isEqualTo("Johnny");
        assertThat(response.getBody().userId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("Given no existing profile, When PUT /api/v1/users/me is called, Then UserProfileNotFoundException is thrown")
    void testUpdateMyProfile_WhenProfileMissing_ThenThrowsNotFoundException() {
        // Given: profile removed
        userProfileRepository.deleteAll();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Ghost", "User", LocalDate.of(2000, 1, 1),
                "Nowhere St", null, "Nowhere", "XX", "00000"
        );

        // When & Then
        assertThatThrownBy(() -> userController.updateMyProfile(request))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/users/me/preferences
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given valid UpdatePreferencesRequest, When PATCH /api/v1/users/me/preferences is called, Then returns HTTP 200 with updated preferences")
    void testUpdateMyPreferences_WhenValidRequest_ThenReturns200WithPreferences() {
        // Given: preferences update request
        UpdatePreferencesRequest request = new UpdatePreferencesRequest("{\"notifications\":true}");

        // When: preferences are updated
        ResponseEntity<UserProfileDto> response = userController.updateMyPreferences(request);

        // Then: 200 OK with new preferences
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().preferences()).isEqualTo("{\"notifications\":true}");
    }

    @Test
    @DisplayName("Given no existing profile, When PATCH /api/v1/users/me/preferences is called, Then UserProfileNotFoundException is thrown")
    void testUpdateMyPreferences_WhenProfileMissing_ThenThrowsNotFoundException() {
        // Given: profile removed
        userProfileRepository.deleteAll();

        UpdatePreferencesRequest request = new UpdatePreferencesRequest("{\"theme\":\"dark\"}");

        // When & Then
        assertThatThrownBy(() -> userController.updateMyPreferences(request))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining(testUserId.toString());
    }
}
