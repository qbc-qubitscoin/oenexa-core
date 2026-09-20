package org.oenexa.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CryptoUtils BDD Test Suite")
class CryptoUtilsTest {

    @Test
    @DisplayName("Given candidate input strings, When sha256 is computed, Then consistent 64-character lowercase hex digest is produced")
    void testSha256() {
        // Given: candidate strings
        String input = "test-string-123";

        // When: SHA-256 digests are computed
        String hash1 = CryptoUtils.sha256(input);
        String hash2 = CryptoUtils.sha256(input);

        // Then: verify format, length, and deterministic consistency
        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2);
        assertTrue(hash1.matches("^[0-9a-f]{64}$"));

        // Given: empty string
        // When: SHA-256 computed
        String emptyHash = CryptoUtils.sha256("");

        // Then: matches standard empty SHA-256 digest
        assertNotNull(emptyHash);
        assertEquals(64, emptyHash.length());
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", emptyHash);
    }

    @Test
    @DisplayName("Given requested byte lengths, When generateRandomToken is invoked, Then cryptographically unique URL-safe tokens are returned")
    void testGenerateRandomToken() {
        // Given: requested byte sizes 16 and 32
        // When: tokens generated
        String token16 = CryptoUtils.generateRandomToken(16);
        String token32 = CryptoUtils.generateRandomToken(32);

        // Then: tokens are non-empty and unique
        assertNotNull(token16);
        assertNotNull(token32);
        assertFalse(token16.isEmpty());
        assertFalse(token32.isEmpty());
        assertNotEquals(token16, token32);

        // Given: another generation call
        // When: token generated
        String anotherToken16 = CryptoUtils.generateRandomToken(16);

        // Then: successive calls produce distinct entropy
        assertNotEquals(token16, anotherToken16);
    }

    @Test
    @DisplayName("Given system request, When generateUuid is called, Then valid RFC-4122 UUID strings are generated")
    void testGenerateUuid() {
        // Given & When: generate UUIDs
        String uuid1 = CryptoUtils.generateUuid();
        String uuid2 = CryptoUtils.generateUuid();

        // Then: canonical format and uniqueness validated
        assertNotNull(uuid1);
        assertNotNull(uuid2);
        assertNotEquals(uuid1, uuid2);
        assertTrue(uuid1.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    @DisplayName("Given CryptoUtils class, When constructor is invoked, Then utility instance is instantiated")
    void testConstructor() {
        // Given & When
        CryptoUtils instance = new CryptoUtils();

        // Then
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Given unsupported algorithm name, When hash is executed, Then wrapped RuntimeException is thrown")
    void testSha256_NoSuchAlgorithmException() {
        // Given - An unsupported algorithm name
        String invalidAlgorithm = "INVALID-ALGO-NONEXISTENT";

        // When & Then - RuntimeException is thrown with descriptive error message and NoSuchAlgorithmException cause
        RuntimeException ex = assertThrows(RuntimeException.class, () -> CryptoUtils.hash("test", invalidAlgorithm));
        assertEquals("SHA-256 algorithm not found", ex.getMessage());
        assertInstanceOf(java.security.NoSuchAlgorithmException.class, ex.getCause());
    }
}
