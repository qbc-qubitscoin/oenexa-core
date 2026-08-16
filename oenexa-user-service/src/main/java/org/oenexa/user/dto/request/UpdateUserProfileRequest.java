package org.oenexa.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request DTO for updating a user's full profile data.
 *
 * <p>All mandatory fields are validated with Jakarta Bean Validation.
 *
 * @param firstName   the user's given name (required, max 100 chars)
 * @param lastName    the user's family name (required, max 100 chars)
 * @param dateOfBirth the user's date of birth (required, must be in the past)
 * @param addressLine1 primary address line (required, max 255 chars)
 * @param addressLine2 optional secondary address line (max 255 chars)
 * @param city        city name (required, max 100 chars)
 * @param country     country name or ISO code (required, max 100 chars)
 * @param postalCode  postal code (required, max 20 chars)
 */
public record UpdateUserProfileRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Address line 1 is required")
        @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
        String addressLine1,

        @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
        String addressLine2,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @NotBlank(message = "Country is required")
        @Size(max = 100, message = "Country must not exceed 100 characters")
        String country,

        @NotBlank(message = "Postal code is required")
        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode

) {}
