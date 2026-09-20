package org.oenexa.payment.service.impl;

import org.oenexa.payment.entity.PaymentEntity;
import org.oenexa.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal CARD_FEE_RATE = new BigDecimal("0.015"); // 1.5%

    @Override
    public BigDecimal calculateFee(PaymentEntity.PaymentType type, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (type == PaymentEntity.PaymentType.CARD) {
            return amount.multiply(CARD_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PaymentEntity initiatePayment(Long userId, PaymentEntity.PaymentType type, PaymentEntity.PaymentDirection direction, BigDecimal amount, String currency) {
        BigDecimal fee = calculateFee(type, amount);

        return PaymentEntity.builder()
                .uuid(UUID.randomUUID().toString())
                .userId(userId)
                .paymentType(type)
                .paymentDirection(direction)
                .amount(amount)
                .currency(currency)
                .fee(fee)
                .status(PaymentEntity.PaymentStatus.INITIATED)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
