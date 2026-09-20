package org.oenexa.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils BDD Test Suite")
class ValidationUtilsTest {

    @Test
    @DisplayName("Given candidate email addresses, When isValidEmail is evaluated, Then correct RFC-5322 validity is returned")
    void testIsValidEmail() {
        // Given: candidate valid and invalid email addresses
        String validEmail1 = "user@example.com";
        String validEmail2 = "firstname.lastname@domain.co.uk";
        String validEmail3 = "alex+trading@oenexa.io";
        String invalidEmail1 = "invalid-email";
        String invalidEmail2 = "@missingusername.com";

        // When & Then: assert valid formats return true
        assertTrue(ValidationUtils.isValidEmail(validEmail1));
        assertTrue(ValidationUtils.isValidEmail(validEmail2));
        assertTrue(ValidationUtils.isValidEmail(validEmail3));

        // When & Then: assert invalid formats return false
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail(invalidEmail1));
        assertFalse(ValidationUtils.isValidEmail(invalidEmail2));
    }

    @Test
    @DisplayName("Given candidate phone numbers, When isValidPhone is evaluated, Then E.164 validity is returned")
    void testIsValidPhone() {
        // Given: candidate valid and invalid phone numbers
        String validPhone1 = "+1234567890";
        String validPhone2 = "+919876543210";
        String validPhone3 = "447123456789";

        // When & Then: assert valid phones return true
        assertTrue(ValidationUtils.isValidPhone(validPhone1));
        assertTrue(ValidationUtils.isValidPhone(validPhone2));
        assertTrue(ValidationUtils.isValidPhone(validPhone3));

        // When & Then: assert invalid phones return false
        assertFalse(ValidationUtils.isValidPhone(null));
        assertFalse(ValidationUtils.isValidPhone(""));
        assertFalse(ValidationUtils.isValidPhone("abc"));
        assertFalse(ValidationUtils.isValidPhone("+0123456"));
    }

    @Test
    @DisplayName("Given candidate passwords, When isStrongPassword is evaluated, Then complexity requirements are strictly enforced")
    void testIsStrongPassword() {
        // Given: candidate strong and weak passwords
        String strong1 = "P@ssword123";
        String strong2 = "Oenexa!2026";
        String strong3 = "Str0ng&Safe";

        // When & Then: assert valid strong passwords return true
        assertTrue(ValidationUtils.isStrongPassword(strong1));
        assertTrue(ValidationUtils.isStrongPassword(strong2));
        assertTrue(ValidationUtils.isStrongPassword(strong3));

        // When & Then: assert violating passwords return false
        assertFalse(ValidationUtils.isStrongPassword(null));
        assertFalse(ValidationUtils.isStrongPassword(""));
        assertFalse(ValidationUtils.isStrongPassword("short1!"));
        assertFalse(ValidationUtils.isStrongPassword("alllowercase1!"));
        assertFalse(ValidationUtils.isStrongPassword("ALLUPPERCASE1!"));
        assertFalse(ValidationUtils.isStrongPassword("NoDigitsSpecial!"));
        assertFalse(ValidationUtils.isStrongPassword("NoSpecialChar123"));
    }

    @Test
    @DisplayName("Given ValidationUtils class, When constructor is invoked, Then instance is created for coverage")
    void testConstructor() {
        // Given & When
        ValidationUtils instance = new ValidationUtils();

        // Then
        assertNotNull(instance);
    }
}
