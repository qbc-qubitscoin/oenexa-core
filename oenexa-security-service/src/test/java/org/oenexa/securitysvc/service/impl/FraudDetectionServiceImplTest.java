package org.oenexa.securitysvc.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FraudDetectionServiceImplTest {

    @Test
    @DisplayName("Given FraudDetectionServiceImpl configuration, When service is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default service configuration

        // When
        FraudDetectionServiceImpl service = new FraudDetectionServiceImpl();

        // Then
        assertNotNull(service);
    }
}
