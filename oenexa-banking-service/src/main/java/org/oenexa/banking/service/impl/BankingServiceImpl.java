package org.oenexa.banking.service.impl;

import org.oenexa.banking.entity.TransferEntity;
import org.oenexa.banking.service.BankingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankingServiceImpl implements BankingService {

    @Override
    public boolean validateIban(String iban) {
        if (iban == null) {
            return false;
        }
        String cleanIban = iban.replaceAll("\\s+", "").toUpperCase();
        if (cleanIban.length() < 15 || cleanIban.length() > 34) {
            return false;
        }
        return cleanIban.matches("^[A-Z]{2}[0-9A-Z]+$");
    }

    @Override
    public TransferEntity initiateTransfer(Long userId, Long bankAccountId, Long beneficiaryId, BigDecimal amount, String currency, TransferEntity.TransferDirection direction, String reference) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }

        LocalDateTime now = LocalDateTime.now();
        return TransferEntity.builder()
                .userId(userId)
                .bankAccountId(bankAccountId)
                .beneficiaryId(beneficiaryId)
                .amount(amount)
                .currency(currency.toUpperCase())
                .direction(direction)
                .status(TransferEntity.TransferStatus.PENDING)
                .reference(reference)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
