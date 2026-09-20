package org.oenexa.audit.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceImplTest {

    @Test
    @DisplayName("Given AuditServiceImpl configuration, When service is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default constructor

        // When
        AuditServiceImpl service = new AuditServiceImpl();

        // Then
        assertNotNull(service);
    }
}
