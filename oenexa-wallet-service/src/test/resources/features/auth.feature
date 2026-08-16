Feature: User Authentication

  Scenario: User registers successfully
    Given a new user with email "bdd@test.com" and password "secret123"
    When the user submits a registration request
    Then the user should receive a valid JWT token
    And the user's email should be "bdd@test.com"
