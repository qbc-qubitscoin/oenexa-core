package org.oenexa.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Thrown when a requested {@link org.oenexa.user.entity.UserProfileEntity} cannot be found.
 *
 * <p>Automatically mapped to HTTP 404 Not Found by {@link GlobalUserExceptionHandler}.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserProfileNotFoundException extends RuntimeException {

    private final UUID userId;

    /**
     * Constructs a {@code UserProfileNotFoundException} for the given user ID.
     *
     * @param userId the UUID of the user whose profile was not found
     */
    public UserProfileNotFoundException(UUID userId) {
        super("User profile not found for userId: " + userId);
        this.userId = userId;
    }

    /**
     * Returns the user ID that triggered this exception.
     *
     * @return the non-null user UUID
     */
    public UUID getUserId() {
        return userId;
    }
}
