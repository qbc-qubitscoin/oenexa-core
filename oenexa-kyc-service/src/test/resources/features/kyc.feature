Feature: KYC Verification Lifecycle

  Scenario: User checks initial KYC status and submits verification
    Given a KYC user is authenticated
    When the user requests their KYC status
    Then the status should be "PENDING"
    When the user submits KYC details with full name "Alice Smith" and country "US"
    Then the status should be "IN_REVIEW"
