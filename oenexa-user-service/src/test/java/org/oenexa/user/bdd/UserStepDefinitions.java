package org.oenexa.user.bdd;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.oenexa.user.UserServiceApplication;
import org.oenexa.user.config.TestConfig;
import org.oenexa.user.controller.UserController;
import org.oenexa.user.dto.request.UpdatePreferencesRequest;
import org.oenexa.user.dto.request.UpdateUserProfileRequest;
import org.oenexa.user.dto.response.UserProfileDto;
import org.oenexa.user.entity.KycLevel;
import org.oenexa.user.entity.UserProfileEntity;
import org.oenexa.user.kafka.consumer.UserEventConsumer;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cucumber step definitions for the User Profile Management feature.
 *
 * <p>Exercises the full vertical slice: Controller → Service → Repository (H2).
 * Kafka event steps call {@link UserEventConsumer} methods directly with JSON payloads.
 * All assertions follow BDD Given-When-Then semantics. Zero Mockito.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = {UserServiceApplication.class, TestConfig.class})
@ActiveProfiles("test")
public class UserStepDefinitions {

    @Autowired
    private UserController userController;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserEventConsumer userEventConsumer;

    private UUID userId;
    private ResponseEntity<UserProfileDto> response;
    private Exception caughtException;

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @Before
    public void beforeScenario() {
        userProfileRepository.deleteAll();
        SecurityContextHolder.clearContext();
        caughtException = null;
        response = null;
    }

    @After
    public void afterScenario() {
        SecurityContextHolder.clearContext();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Given steps
    // ─────────────────────────────────────────────────────────────────────────

    @Given("a registered user profile exists with first name {string} and last name {string}")
    public void a_registered_user_profile_exists(String firstName, String lastName) {
        userId = UUID.randomUUID();
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setFirstName(firstName);
        entity.setLastName(lastName);
        entity.setKycLevel(KycLevel.NONE);
        userProfileRepository.save(entity);

        setAuthContext(userId);
    }

    @Given("no user profile exists for the user")
    public void no_user_profile_exists() {
        userId = UUID.randomUUID();
        setAuthContext(userId);
    }

    @And("the user has KYC level {string}")
    public void the_user_has_kyc_level(String kycLevelStr) {
        KycLevel level = KycLevel.valueOf(kycLevelStr);
        UserProfileEntity entity = userProfileRepository.findById(userId).orElseThrow();
        entity.setKycLevel(level);
        userProfileRepository.save(entity);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // When steps — REST
    // ─────────────────────────────────────────────────────────────────────────

    @When("the user retrieves their profile")
    public void the_user_retrieves_their_profile() {
        try {
            response = userController.getMyProfile();
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("the user updates their profile with first name {string}, last name {string}, and city {string}")
    public void the_user_updates_their_profile(String firstName, String lastName, String city) {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                firstName,
                lastName,
                LocalDate.of(1990, 1, 1),
                "123 Main St",
                null,
                city,
                "UK",
                "SW1A 1AA"
        );
        try {
            response = userController.updateMyProfile(request);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("the user updates their preferences to {string}")
    public void the_user_updates_their_preferences(String preferences) {
        UpdatePreferencesRequest request = new UpdatePreferencesRequest(preferences);
        try {
            response = userController.updateMyPreferences(request);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // When steps — Kafka events
    // ─────────────────────────────────────────────────────────────────────────

    @When("a UserRegisteredEvent is received for the user")
    public void a_user_registered_event_is_received() {
        String eventPayload = """
                {"userId":"%s","email":"test@example.com","phoneNumber":"+441234567890"}
                """.formatted(userId);
        userEventConsumer.handleUserRegistered(eventPayload);
    }

    @When("a KycStatusUpdatedEvent with status {string} is received for the user")
    public void a_kyc_status_updated_event_is_received(String status) {
        String eventPayload = """
                {"userId":"%s","status":"%s","providerReferenceId":"REF-BDD-001"}
                """.formatted(userId, status);
        userEventConsumer.handleKycStatusUpdated(eventPayload);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Then steps — assertions
    // ─────────────────────────────────────────────────────────────────────────

    @Then("the profile first name should be {string} and last name should be {string}")
    public void the_profile_first_name_and_last_name_should_be(String expectedFirst, String expectedLast) {
        assertNotNull(response, "Expected a response but got none");
        assertNotNull(response.getBody(), "Response body must not be null");
        assertEquals(expectedFirst, response.getBody().firstName());
        assertEquals(expectedLast, response.getBody().lastName());
    }

    @Then("the profile first name should be {string} and city should be {string}")
    public void the_profile_first_name_and_city_should_be(String expectedFirst, String expectedCity) {
        assertNotNull(response, "Expected a response but got none");
        assertNotNull(response.getBody(), "Response body must not be null");
        assertEquals(expectedFirst, response.getBody().firstName());
        assertEquals(expectedCity, response.getBody().city());
    }

    @Then("the profile preferences should be {string}")
    public void the_profile_preferences_should_be(String expectedPreferences) {
        assertNotNull(response, "Expected a response but got none");
        assertNotNull(response.getBody(), "Response body must not be null");
        assertEquals(expectedPreferences, response.getBody().preferences());
    }

    @Then("an error indicating {string} should be returned")
    public void an_error_indicating_should_be_returned(String expectedMessage) {
        assertNotNull(caughtException, "Expected an exception but none was thrown");
        assertTrue(caughtException.getMessage().contains(expectedMessage),
                "Expected message to contain '" + expectedMessage + "' but was: " + caughtException.getMessage());
    }

    @Then("a user profile is created with KYC level {string}")
    public void a_user_profile_is_created_with_kyc_level(String expectedLevel) {
        Optional<UserProfileEntity> profile = userProfileRepository.findById(userId);
        assertTrue(profile.isPresent(), "Expected a user profile to be created");
        assertEquals(KycLevel.valueOf(expectedLevel), profile.get().getKycLevel());
    }

    @Then("the profile first name should still be {string}")
    public void the_profile_first_name_should_still_be(String expectedFirstName) {
        UserProfileEntity profile = userProfileRepository.findById(userId).orElseThrow();
        assertEquals(expectedFirstName, profile.getFirstName());
    }

    @Then("the user profile KYC level should be {string}")
    public void the_user_profile_kyc_level_should_be(String expectedLevel) {
        UserProfileEntity profile = userProfileRepository.findById(userId).orElseThrow();
        assertEquals(KycLevel.valueOf(expectedLevel), profile.getKycLevel());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void setAuthContext(UUID uid) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                uid.toString(),
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
