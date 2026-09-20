package org.oenexa.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.user.UserServiceApplication;
import org.oenexa.user.config.TestConfig;
import org.oenexa.user.entity.KycLevel;
import org.oenexa.user.entity.UserProfileEntity;
import org.oenexa.user.kafka.consumer.UserEventConsumer;
import org.oenexa.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * BDD/TDD tests for {@link UserEventConsumer}.
 *
 * <p>Tests invoke consumer methods directly with JSON payloads — no real Kafka broker needed.
 * Kafka listeners are disabled via {@link TestConfig} ({@code autoStartup = false}).
 * All persistence assertions target the H2 in-memory DB via {@link UserProfileRepository}.
 *
 * <p><strong>Coverage:</strong> All branches in {@code handleUserRegistered} and
 * {@code handleKycStatusUpdated}, including the malformed-JSON catch blocks.
 */
@SpringBootTest(classes = {UserServiceApplication.class, TestConfig.class})
@ActiveProfiles("test")
@DisplayName("UserEventConsumer BDD Test Suite")
class UserEventConsumerTest {

    @Autowired
    private UserEventConsumer userEventConsumer;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        userProfileRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleUserRegistered — happy paths
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a UserRegisteredEvent for a new user, When handleUserRegistered is called, Then a profile is created with KycLevel NONE")
    void testHandleUserRegistered_WhenNewUser_ThenProfileCreated() {
        // Given: a valid UserRegisteredEvent JSON for a user that has no profile
        UUID userId = UUID.randomUUID();
        String eventPayload = """
                {"userId":"%s","email":"alice@example.com","phoneNumber":"+441234567890"}
                """.formatted(userId);

        // When: event is handled
        userEventConsumer.handleUserRegistered(eventPayload);

        // Then: profile exists with default KycLevel NONE
        Optional<UserProfileEntity> saved = userProfileRepository.findById(userId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getUserId()).isEqualTo(userId);
        assertThat(saved.get().getKycLevel()).isEqualTo(KycLevel.NONE);
    }

    @Test
    @DisplayName("Given a UserRegisteredEvent for an already-registered user, When handleUserRegistered is called again, Then the existing profile is not overwritten")
    void testHandleUserRegistered_WhenDuplicateEvent_ThenExistingProfilePreserved() {
        // Given: a profile already exists with BASIC KYC
        UUID userId = UUID.randomUUID();
        UserProfileEntity existing = new UserProfileEntity();
        existing.setUserId(userId);
        existing.setFirstName("Original");
        existing.setKycLevel(KycLevel.BASIC);
        userProfileRepository.save(existing);

        String eventPayload = """
                {"userId":"%s","email":"dup@example.com","phoneNumber":"+441234567890"}
                """.formatted(userId);

        // When: duplicate registration event arrives
        userEventConsumer.handleUserRegistered(eventPayload);

        // Then: profile is unchanged — not overwritten
        UserProfileEntity stored = userProfileRepository.findById(userId).orElseThrow();
        assertThat(stored.getFirstName()).isEqualTo("Original");
        assertThat(stored.getKycLevel()).isEqualTo(KycLevel.BASIC);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleUserRegistered — error path
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a malformed JSON payload, When handleUserRegistered is called, Then no exception propagates and no profile is created")
    void testHandleUserRegistered_WhenMalformedJson_ThenNoExceptionAndNoProfile() {
        // Given: invalid JSON that cannot be deserialized
        String badPayload = "this-is-not-json";
        long countBefore = userProfileRepository.count();

        // When & Then: exception is caught internally — no propagation
        assertThatCode(() -> userEventConsumer.handleUserRegistered(badPayload))
                .doesNotThrowAnyException();

        // Then: repository count is unchanged
        assertThat(userProfileRepository.count()).isEqualTo(countBefore);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleKycStatusUpdated — VERIFIED
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a KycStatusUpdatedEvent with status VERIFIED, When handleKycStatusUpdated is called, Then kycLevel is set to BASIC")
    void testHandleKycStatusUpdated_WhenStatusVerified_ThenKycLevelSetToBasic() {
        // Given: an existing profile with KycLevel NONE
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setKycLevel(KycLevel.NONE);
        userProfileRepository.save(entity);

        String eventPayload = """
                {"userId":"%s","status":"VERIFIED","providerReferenceId":"REF-001"}
                """.formatted(userId);

        // When: KYC VERIFIED event is processed
        userEventConsumer.handleKycStatusUpdated(eventPayload);

        // Then: kycLevel is upgraded to BASIC
        UserProfileEntity updated = userProfileRepository.findById(userId).orElseThrow();
        assertThat(updated.getKycLevel()).isEqualTo(KycLevel.BASIC);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // handleKycStatusUpdated — REJECTED
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a KycStatusUpdatedEvent with status REJECTED, When handleKycStatusUpdated is called, Then kycLevel is reset to NONE")
    void testHandleKycStatusUpdated_WhenStatusRejected_ThenKycLevelResetToNone() {
        // Given: an existing profile with KycLevel BASIC
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setKycLevel(KycLevel.BASIC);
        userProfileRepository.save(entity);

        String eventPayload = """
                {"userId":"%s","status":"REJECTED","providerReferenceId":"REF-002"}
                """.formatted(userId);

        // When: KYC REJECTED event is processed
        userEventConsumer.handleKycStatusUpdated(eventPayload);

        // Then: kycLevel is demoted to NONE
        UserProfileEntity updated = userProfileRepository.findById(userId).orElseThrow();
        assertThat(updated.getKycLevel()).isEqualTo(KycLevel.NONE);
    }

    @Test
    @DisplayName("Given a KycStatusUpdatedEvent with an unknown status, When handleKycStatusUpdated is called, Then kycLevel remains unchanged")
    void testHandleKycStatusUpdated_WhenUnknownStatus_ThenKycLevelUnchanged() {
        // Given: a profile with ADVANCED KYC level
        UUID userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setKycLevel(KycLevel.ADVANCED);
        userProfileRepository.save(entity);

        String eventPayload = """
                {"userId":"%s","status":"PENDING","providerReferenceId":"REF-003"}
                """.formatted(userId);

        // When: unknown status event is processed
        userEventConsumer.handleKycStatusUpdated(eventPayload);

        // Then: kycLevel is not modified
        UserProfileEntity stored = userProfileRepository.findById(userId).orElseThrow();
        assertThat(stored.getKycLevel()).isEqualTo(KycLevel.ADVANCED);
    }

    @Test
    @DisplayName("Given a KycStatusUpdatedEvent for a user with no profile, When handleKycStatusUpdated is called, Then no profile is created")
    void testHandleKycStatusUpdated_WhenNoProfileExists_ThenNothingCreated() {
        // Given: non-existent user
        UUID userId = UUID.randomUUID();
        String eventPayload = """
                {"userId":"%s","status":"VERIFIED","providerReferenceId":"REF-004"}
                """.formatted(userId);

        // When: event for non-existent user
        userEventConsumer.handleKycStatusUpdated(eventPayload);

        // Then: no profile was created
        assertThat(userProfileRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Given a malformed JSON KYC event payload, When handleKycStatusUpdated is called, Then no exception propagates")
    void testHandleKycStatusUpdated_WhenMalformedJson_ThenNoExceptionPropagates() {
        // Given: invalid JSON
        String badPayload = "{not-valid-json}";

        // When & Then: exception is swallowed internally
        assertThatCode(() -> userEventConsumer.handleKycStatusUpdated(badPayload))
                .doesNotThrowAnyException();
    }
}
