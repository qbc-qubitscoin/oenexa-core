package org.oenexa.marketdata.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataControllerTest {

    @Test
    @DisplayName("Given MarketDataController instance, When endpoint stubs are queried, Then return expected default values")
    void testEndpoints() {
        // Given
        MarketDataController controller = new MarketDataController();

        // When & Then
        assertNull(controller.getTicker());
        assertNull(controller.getOrderbook());
        assertNull(controller.getTrades());
        assertNull(controller.getKlines());
        assertNull(controller.get24hrStats());
    }
}
