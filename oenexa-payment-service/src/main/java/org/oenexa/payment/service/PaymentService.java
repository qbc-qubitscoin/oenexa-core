package org.oenexa.payment.service;

import org.oenexa.payment.entity.PaymentEntity;
import java.math.BigDecimal;

public interface PaymentService {
    BigDecimal calculateFee(PaymentEntity.PaymentType type, BigDecimal amount);
    PaymentEntity initiatePayment(Long userId, PaymentEntity.PaymentType type, PaymentEntity.PaymentDirection direction, BigDecimal amount, String currency);
}
