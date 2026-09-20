package org.oenexa.banking.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.banking.entity.TransferEntity;
import org.oenexa.banking.service.BankingService;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankingControllerTest {

    static class StubBankingService implements BankingService {
        @Override
        public TransferEntity initiateTransfer(Long userId, Long bankAccountId, Long beneficiaryId, BigDecimal amount, String currency, TransferEntity.TransferDirection direction, String reference) {
            return TransferEntity.builder()
                    .userId(userId)
                    .bankAccountId(bankAccountId)
                    .beneficiaryId(beneficiaryId)
                    .amount(amount)
                    .currency(currency)
                    .direction(direction)
                    .reference(reference)
                    .build();
        }

        @Override
        public boolean validateIban(String iban) {
            return "GB82WEST12345698765432".equals(iban);
        }
    }

    @Test
    @DisplayName("Given valid transfer request parameters, When transfer endpoint is called, Then delegate to BankingService and return 200 OK")
    void testTransfer() {
        // Given - Native stub banking service
        BankingService bankingService = new StubBankingService();
        BankingController controller = new BankingController(bankingService);

        // When
        ResponseEntity<TransferEntity> response = controller.transfer(1L, 2L, 3L, new BigDecimal("100.00"), "GBP", TransferEntity.TransferDirection.OUTBOUND, "REF-1");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getUserId());
        assertEquals(2L, response.getBody().getBankAccountId());
        assertEquals(3L, response.getBody().getBeneficiaryId());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        assertEquals("GBP", response.getBody().getCurrency());
        assertEquals(TransferEntity.TransferDirection.OUTBOUND, response.getBody().getDirection());
        assertEquals("REF-1", response.getBody().getReference());
    }

    @Test
    @DisplayName("Given valid IBAN string, When validateIban endpoint is called, Then delegate to BankingService and return true")
    void testValidateIban() {
        // Given - Native stub banking service
        BankingService bankingService = new StubBankingService();
        BankingController controller = new BankingController(bankingService);

        // When
        ResponseEntity<Boolean> response = controller.validateIban("GB82WEST12345698765432");

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(Boolean.TRUE, response.getBody());
    }
}
