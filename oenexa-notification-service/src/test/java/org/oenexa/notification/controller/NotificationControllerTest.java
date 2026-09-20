package org.oenexa.notification.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationControllerTest {

    @Test
    @DisplayName("Given NotificationController configuration, When controller is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default controller constructor

        // When
        NotificationController controller = new NotificationController();

        // Then
        assertNotNull(controller);
    }
}
