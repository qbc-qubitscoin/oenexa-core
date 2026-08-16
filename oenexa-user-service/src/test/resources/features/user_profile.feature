Feature: User Profile Management
  As a registered user of the OENEXA platform
  I want to manage my profile information and preferences
  So that my account reflects accurate data and personalisation settings

  # ─────────────────────────────────────────────────────────────────────────────
  # Happy-path scenarios — profile CRUD
  # ─────────────────────────────────────────────────────────────────────────────

  Scenario: User fetches their existing profile
    Given a registered user profile exists with first name "Sam" and last name "Taylor"
    When the user retrieves their profile
    Then the profile first name should be "Sam" and last name should be "Taylor"

  Scenario: User updates their profile details
    Given a registered user profile exists with first name "Sam" and last name "Taylor"
    When the user updates their profile with first name "Samuel", last name "Taylor", and city "London"
    Then the profile first name should be "Samuel" and city should be "London"

  Scenario: User updates their preferences
    Given a registered user profile exists with first name "Sam" and last name "Taylor"
    When the user updates their preferences to '{"notifications":true,"theme":"dark"}'
    Then the profile preferences should be '{"notifications":true,"theme":"dark"}'

  # ─────────────────────────────────────────────────────────────────────────────
  # Not-found scenarios
  # ─────────────────────────────────────────────────────────────────────────────

  Scenario: User attempts to fetch a non-existent profile
    Given no user profile exists for the user
    When the user retrieves their profile
    Then an error indicating "User profile not found" should be returned

  Scenario: User attempts to update a non-existent profile
    Given no user profile exists for the user
    When the user updates their profile with first name "Ghost", last name "User", and city "Nowhere"
    Then an error indicating "User profile not found" should be returned

  Scenario: User attempts to update preferences for a non-existent profile
    Given no user profile exists for the user
    When the user updates their preferences to '{"theme":"light"}'
    Then an error indicating "User profile not found" should be returned

  # ─────────────────────────────────────────────────────────────────────────────
  # Kafka event-driven scenarios
  # ─────────────────────────────────────────────────────────────────────────────

  Scenario: Platform creates a default profile when a new user registers
    Given no user profile exists for the user
    When a UserRegisteredEvent is received for the user
    Then a user profile is created with KYC level "NONE"

  Scenario: Platform does not overwrite an existing profile on duplicate registration event
    Given a registered user profile exists with first name "Alice" and last name "Cooper"
    When a UserRegisteredEvent is received for the user
    Then the profile first name should still be "Alice"

  Scenario: Platform upgrades KYC level to BASIC when KYC status is VERIFIED
    Given a registered user profile exists with first name "Sam" and last name "Taylor"
    And the user has KYC level "NONE"
    When a KycStatusUpdatedEvent with status "VERIFIED" is received for the user
    Then the user profile KYC level should be "BASIC"

  Scenario: Platform resets KYC level to NONE when KYC status is REJECTED
    Given a registered user profile exists with first name "Sam" and last name "Taylor"
    And the user has KYC level "BASIC"
    When a KycStatusUpdatedEvent with status "REJECTED" is received for the user
    Then the user profile KYC level should be "NONE"
