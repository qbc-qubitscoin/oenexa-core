package org.oenexa.risk.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceImplTest {

    @Test
    @DisplayName("Given RiskScoringServiceImpl instance, When risk score methods are invoked, Then execute without throwing exceptions")
    void testRiskScoring() {
        // Given
        RiskScoringServiceImpl service = new RiskScoringServiceImpl();
        assertNotNull(service);

        // When & Then
        assertDoesNotThrow(service::calculateLoginRisk);
        assertDoesNotThrow(service::calculateTransactionRisk);
        assertDoesNotThrow(service::calculateWithdrawalRisk);
    }
}
