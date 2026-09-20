package org.oenexa.banking.controller;

import lombok.RequiredArgsConstructor;
import org.oenexa.banking.entity.TransferEntity;
import org.oenexa.banking.service.BankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/banking")
@RequiredArgsConstructor
public class BankingController {

    private final BankingService bankingService;

    @PostMapping("/transfers")
    public ResponseEntity<TransferEntity> transfer(
            @RequestParam Long userId,
            @RequestParam Long bankAccountId,
            @RequestParam Long beneficiaryId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestParam TransferEntity.TransferDirection direction,
            @RequestParam(required = false) String reference) {
        return ResponseEntity.ok(bankingService.initiateTransfer(userId, bankAccountId, beneficiaryId, amount, currency, direction, reference));
    }

    @GetMapping("/iban/validate")
    public ResponseEntity<Boolean> validateIban(@RequestParam String iban) {
        return ResponseEntity.ok(bankingService.validateIban(iban));
    }
}
