package org.oenexa.audit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditControllerTest {

    @Test
    @DisplayName("Given AuditController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        AuditController controller = new AuditController();

        // Then
        assertNotNull(controller);
    }
}
