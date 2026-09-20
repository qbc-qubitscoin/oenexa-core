package org.oenexa.wallet.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oenexa.wallet.entity.WalletEntity;
import org.oenexa.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(WalletService.class)
@DisplayName("WalletService BDD Test Suite")
class WalletServiceTest {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @Test
    @DisplayName("Given existing user wallets in repository, When getWallets is invoked, Then list of user wallets is returned")
    void testGetWallets() {
        // Given: wallet saved in repository
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(1L);
        wallet.setCurrency("USD");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setWalletType(WalletEntity.WalletType.SPOT);
        wallet.setStatus(WalletEntity.WalletStatus.ACTIVE);
        wallet.setIsDefault(true);
        walletRepository.save(wallet);

        // When: getWallets is called
        List<WalletEntity> wallets = walletService.getWallets(1L);

        // Then: matching wallet returned
        assertFalse(wallets.isEmpty());
        assertEquals(1, wallets.size());
        assertEquals("USD", wallets.get(0).getCurrency());
    }

    @Test
    @DisplayName("Given existing wallet with initial balance, When addBalance is called, Then balance is incremented accurately")
    void testAddBalance_ExistingWallet() {
        // Given: existing wallet with balance of 100
        WalletEntity wallet = new WalletEntity();
        wallet.setUserId(1L);
        wallet.setCurrency("USD");
        wallet.setBalance(BigDecimal.valueOf(100));
        wallet.setWalletType(WalletEntity.WalletType.SPOT);
        wallet.setStatus(WalletEntity.WalletStatus.ACTIVE);
        wallet.setIsDefault(true);
        walletRepository.save(wallet);

        // When: addBalance is called with 50
        walletService.addBalance(1L, "USD", BigDecimal.valueOf(50));

        // Then: updated balance is 150
        WalletEntity updatedWallet = walletRepository.findByUserIdAndCurrency(1L, "USD").orElseThrow();
        assertEquals(0, BigDecimal.valueOf(150).compareTo(updatedWallet.getBalance()));
    }

    @Test
    @DisplayName("Given no existing wallet for user and currency, When addBalance is called, Then new wallet is provisioned with balance")
    void testAddBalance_NewWallet() {
        // Given: no existing wallet for EUR

        // When: addBalance is called for new currency
        walletService.addBalance(1L, "EUR", BigDecimal.valueOf(50));

        // Then: new wallet created with SPOT type and correct balance
        WalletEntity newWallet = walletRepository.findByUserIdAndCurrency(1L, "EUR").orElseThrow();
        assertEquals(0, BigDecimal.valueOf(50).compareTo(newWallet.getBalance()));
        assertEquals(WalletEntity.WalletType.SPOT, newWallet.getWalletType());
    }
}
