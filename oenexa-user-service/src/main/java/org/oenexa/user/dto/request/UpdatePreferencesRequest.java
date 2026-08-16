package org.oenexa.user.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a user's preferences JSON blob.
 *
 * @param preferences JSON string of user preferences (required, may be "{}" but not null)
 */
public record UpdatePreferencesRequest(

        @NotNull(message = "Preferences must not be null")
        String preferences

) {}
