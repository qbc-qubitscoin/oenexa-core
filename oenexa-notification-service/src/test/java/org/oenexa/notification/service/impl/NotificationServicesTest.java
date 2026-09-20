package org.oenexa.notification.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServicesTest {

    @Test
    @DisplayName("Given notification service configurations, When service implementations are instantiated, Then all services initialize successfully")
    void testServiceInitializations() {
        // Given - Default service implementations

        // When
        NotificationServiceImpl notificationService = new NotificationServiceImpl();
        EmailServiceImpl emailService = new EmailServiceImpl();
        SmsServiceImpl smsService = new SmsServiceImpl();

        // Then
        assertNotNull(notificationService);
        assertNotNull(emailService);
        assertNotNull(smsService);
    }
}
