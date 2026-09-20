package org.oenexa.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtProperties BDD Test Suite")
class JwtPropertiesTest {

    @Test
    @DisplayName("Given JwtProperties class, When values are set and inspected, Then defaults and customized properties are accurately preserved")
    void testJwtProperties() {
        // Given: fresh JwtProperties instance
        JwtProperties properties = new JwtProperties();

        // When & Then: verify defaults
        assertEquals("oenexa", properties.getIssuer());
        assertEquals(900000, properties.getAccessTokenExpiration());
        assertEquals(604800000, properties.getRefreshTokenExpiration());
        assertNull(properties.getSecret());

        // When: custom properties set
        properties.setSecret("my-super-secret-key-123456789012345678901234567890");
        properties.setIssuer("custom-issuer");
        properties.setAccessTokenExpiration(3600000);
        properties.setRefreshTokenExpiration(86400000);

        // Then: getters reflect mutated values
        assertEquals("my-super-secret-key-123456789012345678901234567890", properties.getSecret());
        assertEquals("custom-issuer", properties.getIssuer());
        assertEquals(3600000, properties.getAccessTokenExpiration());
        assertEquals(86400000, properties.getRefreshTokenExpiration());

        // Then: equals and hashCode match identical object
        assertNotNull(properties.toString());
        JwtProperties same = new JwtProperties();
        same.setSecret("my-super-secret-key-123456789012345678901234567890");
        same.setIssuer("custom-issuer");
        same.setAccessTokenExpiration(3600000);
        same.setRefreshTokenExpiration(86400000);
        assertEquals(properties, same);
        assertEquals(properties.hashCode(), same.hashCode());
    }
}
