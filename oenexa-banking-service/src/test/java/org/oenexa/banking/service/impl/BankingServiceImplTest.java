package org.oenexa.banking.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.banking.entity.TransferEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BankingServiceImplTest {

    private BankingServiceImpl bankingService;

    @BeforeEach
    void setUp() {
        bankingService = new BankingServiceImpl();
    }

    @Test
    @DisplayName("Given valid and invalid IBAN strings, When validateIban is invoked, Then return true only for valid IBAN formats")
    void testValidateIban() {
        // Given & When & Then
        // Valid IBANs
        assertTrue(bankingService.validateIban("GB82WEST12345698765432"));
        assertTrue(bankingService.validateIban("de89370400440532013000"));
        assertTrue(bankingService.validateIban("FR1420041010050500013M02606"));
        assertTrue(bankingService.validateIban(" GB 82 WEST 1234 5698 7654 32 "));

        // Invalid IBANs
        assertFalse(bankingService.validateIban(null));
        assertFalse(bankingService.validateIban(""));
        assertFalse(bankingService.validateIban("GB12")); // Too short (< 15)
        assertFalse(bankingService.validateIban("GB82WEST1234569876543212345678901234567890")); // Too long (> 34)
        assertFalse(bankingService.validateIban("1282WEST12345698765432")); // Doesn't start with 2 letters
        assertFalse(bankingService.validateIban("GB--WEST12345698765432")); // Contains invalid characters
    }

    @Test
    @DisplayName("Given valid transfer inputs, When initiateTransfer is called, Then create TransferEntity with PENDING status")
    void testInitiateTransfer_Success() {
        // Given
        Long userId = 100L;
        Long bankAccountId = 1L;
        Long beneficiaryId = 2L;
        BigDecimal amount = new BigDecimal("500.00");
        String currency = "EUR";
        TransferEntity.TransferDirection direction = TransferEntity.TransferDirection.OUTBOUND;
        String reference = "REF-1234";

        // When
        TransferEntity transfer = bankingService.initiateTransfer(
                userId,
                bankAccountId,
                beneficiaryId,
                amount,
                currency,
                direction,
                reference
        );

        // Then
        assertNotNull(transfer);
        assertEquals(100L, transfer.getUserId());
        assertEquals(1L, transfer.getBankAccountId());
        assertEquals(2L, transfer.getBeneficiaryId());
        assertEquals(new BigDecimal("500.00"), transfer.getAmount());
        assertEquals("EUR", transfer.getCurrency());
        assertEquals(TransferEntity.TransferDirection.OUTBOUND, transfer.getDirection());
        assertEquals(TransferEntity.TransferStatus.PENDING, transfer.getStatus());
        assertEquals("REF-1234", transfer.getReference());
        assertNotNull(transfer.getCreatedAt());
        assertNotNull(transfer.getUpdatedAt());
    }

    @Test
    @DisplayName("Given invalid transfer amount or currency, When initiateTransfer is called, Then IllegalArgumentException is thrown")
    void testInitiateTransfer_Validation() {
        // Given invalid arguments (null amount, non-positive amount, null/blank currency)

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                bankingService.initiateTransfer(1L, 1L, 2L, null, "USD", TransferEntity.TransferDirection.INBOUND, "REF"));

        assertThrows(IllegalArgumentException.class, () ->
                bankingService.initiateTransfer(1L, 1L, 2L, BigDecimal.ZERO, "USD", TransferEntity.TransferDirection.INBOUND, "REF"));

        assertThrows(IllegalArgumentException.class, () ->
                bankingService.initiateTransfer(1L, 1L, 2L, new BigDecimal("-50.00"), "USD", TransferEntity.TransferDirection.INBOUND, "REF"));

        assertThrows(IllegalArgumentException.class, () ->
                bankingService.initiateTransfer(1L, 1L, 2L, new BigDecimal("50.00"), null, TransferEntity.TransferDirection.INBOUND, "REF"));

        assertThrows(IllegalArgumentException.class, () ->
                bankingService.initiateTransfer(1L, 1L, 2L, new BigDecimal("50.00"), "   ", TransferEntity.TransferDirection.INBOUND, "REF"));
    }
}
