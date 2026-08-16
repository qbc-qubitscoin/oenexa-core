package org.oenexa.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.user.UserServiceApplication;
import org.oenexa.user.config.TestConfig;
import org.oenexa.user.dto.request.UpdatePreferencesRequest;
import org.oenexa.user.dto.request.UpdateUserProfileRequest;
import org.oenexa.user.dto.response.UserProfileDto;
import org.oenexa.user.entity.KycLevel;
import org.oenexa.user.entity.UserProfileEntity;
import org.oenexa.user.exception.UserProfileNotFoundException;
import org.oenexa.user.repository.UserProfileRepository;
import org.oenexa.user.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * BDD/TDD unit tests for {@link UserServiceImpl}.
 *
 * <p>Uses real H2 in-memory database via Spring Boot test slice — zero Mockito.
 * All tests follow Given-When-Then semantics with 100% JaCoCo coverage.
 */
@SpringBootTest(classes = {UserServiceApplication.class, TestConfig.class})
@ActiveProfiles("test")
@DisplayName("UserServiceImpl BDD Test Suite")
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        userProfileRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getUserProfile
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given existing user profile in DB, When getUserProfile is called, Then profile DTO is returned with correct fields")
    void testGetUserProfile_WhenProfileExists_ThenReturnDto() {
        // Given: persisted profile with known attributes
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setFirstName("Alice");
        entity.setLastName("Wonderland");
        entity.setKycLevel(KycLevel.BASIC);
        userProfileRepository.save(entity);

        // When: profile is retrieved by user ID
        UserProfileDto profile = userService.getUserProfile(userId);

        // Then: returned DTO matches persisted entity
        assertThat(profile).isNotNull();
        assertThat(profile.userId()).isEqualTo(userId);
        assertThat(profile.firstName()).isEqualTo("Alice");
        assertThat(profile.lastName()).isEqualTo("Wonderland");
        assertThat(profile.kycLevel()).isEqualTo(KycLevel.BASIC);
    }

    @Test
    @DisplayName("Given non-existent userId, When getUserProfile is called, Then UserProfileNotFoundException is thrown")
    void testGetUserProfile_WhenNotFound_ThenThrowsUserProfileNotFoundException() {
        // Given: a userId that has no corresponding profile
        UUID userId = UUID.randomUUID();

        // When & Then: typed domain exception is thrown with userId in message
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining("User profile not found")
                .hasMessageContaining(userId.toString());
    }

    @Test
    @DisplayName("Given non-existent userId, When getUserProfile is called, Then exception carries the correct userId")
    void testGetUserProfile_WhenNotFound_ThenExceptionCarriesUserId() {
        // Given
        UUID userId = UUID.randomUUID();

        // When & Then: exception has the exact userId
        assertThatThrownBy(() -> userService.getUserProfile(userId))
                .isInstanceOf(UserProfileNotFoundException.class)
                .satisfies(ex -> assertThat(((UserProfileNotFoundException) ex).getUserId()).isEqualTo(userId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateUserProfile
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given existing profile, When updateUserProfile is called with full request, Then all fields are updated and persisted")
    void testUpdateUserProfile_WhenProfileExists_ThenAllFieldsUpdated() {
        // Given: a persisted profile
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        userProfileRepository.save(entity);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Bob",
                "Builder",
                LocalDate.of(1988, 8, 8),
                "123 Construction Way",
                "Apt 4B",
                "London",
                "UK",
                "EC1A 1BB"
        );

        // When: profile update is applied
        UserProfileDto updated = userService.updateUserProfile(userId, request);

        // Then: all fields match the request
        assertThat(updated).isNotNull();
        assertThat(updated.firstName()).isEqualTo("Bob");
        assertThat(updated.lastName()).isEqualTo("Builder");
        assertThat(updated.dateOfBirth()).isEqualTo(LocalDate.of(1988, 8, 8));
        assertThat(updated.addressLine1()).isEqualTo("123 Construction Way");
        assertThat(updated.addressLine2()).isEqualTo("Apt 4B");
        assertThat(updated.city()).isEqualTo("London");
        assertThat(updated.country()).isEqualTo("UK");
        assertThat(updated.postalCode()).isEqualTo("EC1A 1BB");
    }

    @Test
    @DisplayName("Given existing profile, When updateUserProfile with null addressLine2, Then addressLine2 is set to null")
    void testUpdateUserProfile_WhenAddressLine2IsNull_ThenNullPersisted() {
        // Given
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setAddressLine2("Old Line 2");
        userProfileRepository.save(entity);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Jane", "Doe", LocalDate.of(1990, 3, 15),
                "1 Oxford St", null, "London", "UK", "W1A 1AA"
        );

        // When
        UserProfileDto updated = userService.updateUserProfile(userId, request);

        // Then: addressLine2 is cleared
        assertThat(updated.addressLine2()).isNull();
    }

    @Test
    @DisplayName("Given non-existent userId, When updateUserProfile is called, Then UserProfileNotFoundException is thrown")
    void testUpdateUserProfile_WhenNotFound_ThenThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Bob", "Builder", LocalDate.now().minusDays(1), "123 Way", null, "City", "Country", "12345"
        );

        // When & Then
        assertThatThrownBy(() -> userService.updateUserProfile(userId, request))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updatePreferences
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given existing profile, When updatePreferences is called, Then preferences JSON is updated")
    void testUpdatePreferences_WhenProfileExists_ThenPreferencesUpdated() {
        // Given: a persisted profile
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        userProfileRepository.save(entity);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest("{\"theme\":\"dark\",\"currency\":\"USD\"}");

        // When: preferences are updated
        UserProfileDto updated = userService.updatePreferences(userId, request);

        // Then: preferences JSON matches
        assertThat(updated).isNotNull();
        assertThat(updated.preferences()).isEqualTo("{\"theme\":\"dark\",\"currency\":\"USD\"}");
    }

    @Test
    @DisplayName("Given existing profile with prior preferences, When updatePreferences is called again, Then preferences are overwritten")
    void testUpdatePreferences_WhenCalledTwice_ThenPreferencesOverwritten() {
        // Given: a profile with initial preferences
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setPreferences("{\"theme\":\"light\"}");
        userProfileRepository.save(entity);

        UpdatePreferencesRequest request = new UpdatePreferencesRequest("{\"theme\":\"dark\"}");

        // When
        UserProfileDto updated = userService.updatePreferences(userId, request);

        // Then: new preferences replace old
        assertThat(updated.preferences()).isEqualTo("{\"theme\":\"dark\"}");
    }

    @Test
    @DisplayName("Given non-existent userId, When updatePreferences is called, Then UserProfileNotFoundException is thrown")
    void testUpdatePreferences_WhenNotFound_ThenThrowsException() {
        // Given
        UUID userId = UUID.randomUUID();
        UpdatePreferencesRequest request = new UpdatePreferencesRequest("{\"theme\":\"dark\"}");

        // When & Then
        assertThatThrownBy(() -> userService.updatePreferences(userId, request))
                .isInstanceOf(UserProfileNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // KycLevel default
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a newly created profile, When getUserProfile is called, Then kycLevel defaults to NONE")
    void testGetUserProfile_WhenNewProfile_ThenKycLevelDefaultsToNone() {
        // Given: minimal profile (no KYC set)
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        userProfileRepository.save(entity);

        // When
        UserProfileDto profile = userService.getUserProfile(userId);

        // Then: default KYC level is NONE
        assertThat(profile.kycLevel()).isEqualTo(KycLevel.NONE);
    }
}
