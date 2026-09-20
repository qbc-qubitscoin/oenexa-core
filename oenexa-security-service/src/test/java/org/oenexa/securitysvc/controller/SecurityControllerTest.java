package org.oenexa.securitysvc.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityControllerTest {

    @Test
    @DisplayName("Given SecurityController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        SecurityController controller = new SecurityController();

        // Then
        assertNotNull(controller);
    }
}
