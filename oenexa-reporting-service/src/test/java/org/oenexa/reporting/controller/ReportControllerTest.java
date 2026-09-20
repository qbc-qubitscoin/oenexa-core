package org.oenexa.reporting.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportControllerTest {

    @Test
    @DisplayName("Given ReportController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        ReportController controller = new ReportController();

        // Then
        assertNotNull(controller);
    }
}
