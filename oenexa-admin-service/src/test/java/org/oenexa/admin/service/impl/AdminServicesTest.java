package org.oenexa.admin.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminServicesTest {

    @Test
    @DisplayName("Given admin service configurations, When service implementations are instantiated, Then all services initialize successfully")
    void testServices() {
        // Given & When & Then
        assertNotNull(new AdminKycServiceImpl());
        assertNotNull(new AdminTransactionServiceImpl());
        assertNotNull(new AdminUserServiceImpl());
    }
}
