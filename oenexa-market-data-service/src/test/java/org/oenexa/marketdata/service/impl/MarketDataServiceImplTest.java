package org.oenexa.marketdata.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataServiceImplTest {

    @Test
    @DisplayName("Given MarketDataServiceImpl configuration, When service is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Component configuration

        // When
        MarketDataServiceImpl service = new MarketDataServiceImpl();

        // Then
        assertNotNull(service);
    }
}
