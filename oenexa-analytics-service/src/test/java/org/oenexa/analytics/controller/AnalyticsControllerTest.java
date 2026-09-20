package org.oenexa.analytics.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsControllerTest {

    @Test
    @DisplayName("Given AnalyticsController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        AnalyticsController controller = new AnalyticsController();

        // Then
        assertNotNull(controller);
    }
}
