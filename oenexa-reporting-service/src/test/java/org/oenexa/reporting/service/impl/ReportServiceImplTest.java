package org.oenexa.reporting.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportServiceImplTest {

    @Test
    @DisplayName("Given ReportServiceImpl instance, When report generation methods are called, Then execute without throwing exceptions")
    void testReportService() {
        // Given
        ReportServiceImpl service = new ReportServiceImpl();
        assertNotNull(service);

        // When & Then
        assertDoesNotThrow(service::generateFinancialReport);
        assertDoesNotThrow(service::generateTaxReport);
        assertDoesNotThrow(service::generateComplianceReport);
    }
}
