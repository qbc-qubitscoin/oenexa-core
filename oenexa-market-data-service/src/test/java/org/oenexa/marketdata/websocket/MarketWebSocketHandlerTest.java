package org.oenexa.marketdata.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarketWebSocketHandlerTest {

    @Test
    @DisplayName("Given MarketWebSocketHandler configuration, When handler is instantiated, Then initialize successfully")
    void testInitialization() {
        // Given - Default handler configuration

        // When
        MarketWebSocketHandler handler = new MarketWebSocketHandler();

        // Then
        assertNotNull(handler);
    }
}
