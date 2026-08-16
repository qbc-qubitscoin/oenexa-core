package org.oenexa.identity.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.identity.service.impl.MockNotificationServiceImpl;
import org.oenexa.identity.service.impl.SendGridEmailService;
import org.oenexa.identity.service.impl.TwilioSmsService;

@DisplayName("NotificationServices BDD Test Suite")
public class NotificationServicesTest {

    @Test
    @DisplayName("Given MockNotificationService, When sendEmailOtp and sendSmsOtp are invoked, Then OTP messages are processed")
    void testMockNotificationService() {
        // Given: MockNotificationService instance
        MockNotificationServiceImpl mockService = new MockNotificationServiceImpl();

        // When & Then: methods execute cleanly
        mockService.sendEmailOtp("test@example.com", "123456");
        mockService.sendSmsOtp("+1234567890", "123456");
    }

    @Test
    @DisplayName("Given SendGridEmailService, When sendEmailOtp and sendSmsOtp are invoked, Then email notifications are dispatched")
    void testSendGridEmailService() {
        // Given: SendGridEmailService instance
        SendGridEmailService sendGridService = new SendGridEmailService();

        // When & Then: dispatch succeeds
        sendGridService.sendEmailOtp("test@example.com", "123456");
        sendGridService.sendSmsOtp("+1234567890", "123456");
    }

    @Test
    @DisplayName("Given TwilioSmsService, When sendEmailOtp and sendSmsOtp are invoked, Then SMS notifications are dispatched")
    void testTwilioSmsService() {
        // Given: TwilioSmsService instance
        TwilioSmsService twilioService = new TwilioSmsService();

        // When & Then: dispatch succeeds
        twilioService.sendEmailOtp("test@example.com", "123456");
        twilioService.sendSmsOtp("+1234567890", "123456");
    }
}
