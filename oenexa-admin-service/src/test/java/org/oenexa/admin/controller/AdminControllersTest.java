package org.oenexa.admin.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminControllersTest {

    @Test
    @DisplayName("Given admin controller configurations, When controllers are instantiated, Then all controllers initialize successfully")
    void testControllers() {
        // Given & When & Then
        assertNotNull(new AdminKycController());
        assertNotNull(new AdminSystemController());
        assertNotNull(new AdminTransactionController());
        assertNotNull(new AdminUserController());
    }
}
