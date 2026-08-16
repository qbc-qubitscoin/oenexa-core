package org.oenexa.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.user.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context integration smoke test for the User Service.
 *
 * <p>Verifies the full application context loads cleanly with the test profile
 * (H2 in-memory DB, Flyway disabled, Kafka auto-startup disabled, Redis disabled).
 */
@SpringBootTest(classes = {UserServiceApplication.class, TestConfig.class})
@ActiveProfiles("test")
@DisplayName("UserService Context Integration Test")
class UserServiceIntegrationTest {

    @Test
    @DisplayName("Given Spring test configuration, When application context is loaded, Then context loads successfully with no startup errors")
    void contextLoads() {
        // Given: test profile active — H2, no Flyway, Kafka disabled

        // When: Spring context is bootstrapped

        // Then: context loads cleanly with no exceptions
    }
}
