package org.oenexa.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a user's profile in the {@code user_profiles} table.
 *
 * <p>Uses {@code TEXT} for the {@code preferences} column for H2 and MySQL compatibility.
 * Optimistic locking is enforced via {@code @Version}.
 */
@Setter
@Getter
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    private UUID userId;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String postalCode;

    /** Stored as TEXT for cross-database compatibility (H2 and MySQL). */
    @Column(columnDefinition = "TEXT")
    private String preferences;

    @Enumerated(EnumType.STRING)
    private KycLevel kycLevel = KycLevel.NONE;

    /** Optimistic locking version column. */
    @Version
    private Long version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** JPA requires a public no-args constructor. */
    public UserProfileEntity() {}

    @PrePersist
    void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
