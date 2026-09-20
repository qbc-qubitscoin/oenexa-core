package org.oenexa.risk.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskControllerTest {

    @Test
    @DisplayName("Given RiskController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        RiskController controller = new RiskController();

        // Then
        assertNotNull(controller);
    }
}
