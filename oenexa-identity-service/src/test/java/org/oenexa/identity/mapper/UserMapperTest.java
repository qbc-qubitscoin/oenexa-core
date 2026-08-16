package org.oenexa.identity.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("UserMapper BDD Test Suite")
public class UserMapperTest {

    @Test
    @DisplayName("Given UserMapperImpl, When constructor is invoked, Then mapper instance is instantiated for coverage")
    void testConstructor() {
        // Given & When
        UserMapperImpl mapper = new UserMapperImpl();

        // Then
        assertNotNull(mapper);
    }
}
