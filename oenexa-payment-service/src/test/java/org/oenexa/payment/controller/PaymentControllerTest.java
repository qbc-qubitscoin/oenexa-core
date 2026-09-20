package org.oenexa.payment.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.payment.entity.PaymentEntity;
import org.oenexa.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentControllerTest {

    static class StubPaymentService implements PaymentService {
        @Override
        public BigDecimal calculateFee(PaymentEntity.PaymentType type, BigDecimal amount) {
            return BigDecimal.ZERO;
        }

        @Override
        public PaymentEntity initiatePayment(Long userId, PaymentEntity.PaymentType type, PaymentEntity.PaymentDirection direction, BigDecimal amount, String currency) {
            return PaymentEntity.builder()
                    .userId(userId)
                    .paymentType(type)
                    .paymentDirection(direction)
                    .amount(amount)
                    .currency(currency)
                    .build();
        }
    }

    @Test
    @DisplayName("Given valid payment initiation arguments, When initiate endpoint is called, Then delegate to PaymentService and return 200 OK")
    void testInitiate() {
        // Given - Native stub payment service
        PaymentService paymentService = new StubPaymentService();
        PaymentController controller = new PaymentController(paymentService);

        // When
        ResponseEntity<PaymentEntity> response = controller.initiate(1L, PaymentEntity.PaymentType.SEPA, PaymentEntity.PaymentDirection.INBOUND, new BigDecimal("100.00"), "EUR");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals(PaymentEntity.PaymentType.SEPA, response.getBody().getPaymentType());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        assertEquals("EUR", response.getBody().getCurrency());
    }
}
