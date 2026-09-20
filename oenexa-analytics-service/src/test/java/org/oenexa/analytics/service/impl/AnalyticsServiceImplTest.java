package org.oenexa.analytics.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceImplTest {

    @Test
    @DisplayName("Given AnalyticsServiceImpl instance, When analytics metric methods are called, Then execute without throwing exceptions")
    void testAnalyticsService() {
        // Given
        AnalyticsServiceImpl service = new AnalyticsServiceImpl();
        assertNotNull(service);

        // When & Then
        assertDoesNotThrow(service::platformMetrics);
        assertDoesNotThrow(service::userEngagement);
        assertDoesNotThrow(service::tradingVolume);
        assertDoesNotThrow(service::revenue);
    }
}
