package org.oenexa.payment.controller;

import lombok.RequiredArgsConstructor;
import org.oenexa.payment.entity.PaymentEntity;
import org.oenexa.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentEntity> initiate(
            @RequestParam Long userId,
            @RequestParam PaymentEntity.PaymentType type,
            @RequestParam PaymentEntity.PaymentDirection direction,
            @RequestParam BigDecimal amount,
            @RequestParam String currency) {
        return ResponseEntity.ok(paymentService.initiatePayment(userId, type, direction, amount, currency));
    }
}
