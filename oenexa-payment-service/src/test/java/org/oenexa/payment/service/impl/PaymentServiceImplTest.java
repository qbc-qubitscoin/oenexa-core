package org.oenexa.payment.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.payment.entity.PaymentEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceImplTest {

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl();
    }

    @Test
    @DisplayName("Given payment types and positive amount, When calculateFee is invoked, Then CARD has 1.5% fee and SEPA/ACH have zero fee")
    void testCalculateFee() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");

        // When
        BigDecimal cardFee = paymentService.calculateFee(PaymentEntity.PaymentType.CARD, amount);
        BigDecimal sepaFee = paymentService.calculateFee(PaymentEntity.PaymentType.SEPA, amount);
        BigDecimal achFee = paymentService.calculateFee(PaymentEntity.PaymentType.ACH, new BigDecimal("250.00"));

        // Then
        assertEquals(new BigDecimal("1.50"), cardFee);
        assertEquals(new BigDecimal("0.00"), sepaFee);
        assertEquals(new BigDecimal("0.00"), achFee);
    }

    @Test
    @DisplayName("Given null or non-positive amount, When calculateFee is called, Then IllegalArgumentException is thrown")
    void testCalculateFee_InvalidAmount() {
        // Given invalid amounts (null, zero, negative)

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                paymentService.calculateFee(PaymentEntity.PaymentType.CARD, null));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.calculateFee(PaymentEntity.PaymentType.CARD, BigDecimal.ZERO));

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.calculateFee(PaymentEntity.PaymentType.CARD, new BigDecimal("-10.00")));
    }

    @Test
    @DisplayName("Given valid initiation parameters, When initiatePayment is invoked, Then PaymentEntity is created with fee and INITIATED status")
    void testInitiatePayment() {
        // Given
        Long userId = 10L;
        PaymentEntity.PaymentType type = PaymentEntity.PaymentType.CARD;
        PaymentEntity.PaymentDirection direction = PaymentEntity.PaymentDirection.INBOUND;
        BigDecimal amount = new BigDecimal("200.00");
        String currency = "USD";

        // When
        PaymentEntity payment = paymentService.initiatePayment(userId, type, direction, amount, currency);

        // Then
        assertNotNull(payment);
        assertNotNull(payment.getUuid());
        assertEquals(10L, payment.getUserId());
        assertEquals(PaymentEntity.PaymentType.CARD, payment.getPaymentType());
        assertEquals(PaymentEntity.PaymentDirection.INBOUND, payment.getPaymentDirection());
        assertEquals(new BigDecimal("200.00"), payment.getAmount());
        assertEquals("USD", payment.getCurrency());
        assertEquals(new BigDecimal("3.00"), payment.getFee());
        assertEquals(PaymentEntity.PaymentStatus.INITIATED, payment.getStatus());
        assertNotNull(payment.getCreatedAt());
    }
}
