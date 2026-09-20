package org.oenexa.banking.service;

import org.oenexa.banking.entity.TransferEntity;
import java.math.BigDecimal;

public interface BankingService {
    TransferEntity initiateTransfer(Long userId, Long bankAccountId, Long beneficiaryId, BigDecimal amount, String currency, TransferEntity.TransferDirection direction, String reference);
    boolean validateIban(String iban);
}
