package org.oenexa.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtils BDD Test Suite")
class DateUtilsTest {

    @Test
    @DisplayName("Given LocalDateTime instance or null, When format is invoked, Then standard formatted string or null is returned")
    void testFormat() {
        // Given: valid LocalDateTime and null
        LocalDateTime dt = LocalDateTime.of(2026, 9, 19, 14, 30, 45);

        // When: format is invoked
        String formatted = DateUtils.format(dt);
        String nullFormatted = DateUtils.format(null);

        // Then: standard yyyy-MM-dd HH:mm:ss format or null returned
        assertEquals("2026-09-19 14:30:45", formatted);
        assertNull(nullFormatted);
    }

    @Test
    @DisplayName("Given candidate date strings, When parse is invoked, Then correct LocalDateTime or null is returned")
    void testParse() {
        // Given: formatted date string
        String dateStr = "2026-09-19 14:30:45";

        // When: parse is called
        LocalDateTime parsed = DateUtils.parse(dateStr);

        // Then: components match expected date-time values
        assertNotNull(parsed);
        assertEquals(2026, parsed.getYear());
        assertEquals(9, parsed.getMonthValue());
        assertEquals(19, parsed.getDayOfMonth());
        assertEquals(14, parsed.getHour());
        assertEquals(30, parsed.getMinute());
        assertEquals(45, parsed.getSecond());

        // Given: null and empty strings
        // When & Then: graceful null returns
        assertNull(DateUtils.parse(null));
        assertNull(DateUtils.parse(""));
    }

    @Test
    @DisplayName("Given epoch millisecond timestamps, When converted to LocalDateTime and back, Then bidirectional round-trip succeeds")
    void testEpochMilliConversions() {
        // Given: current epoch millis
        long currentMillis = System.currentTimeMillis();

        // When: converted to LocalDateTime
        LocalDateTime dt = DateUtils.fromEpochMilli(currentMillis);
        assertNotNull(dt);

        // When: converted back to epoch millis
        long convertedBack = DateUtils.toEpochMilli(dt);

        // Then: within millisecond precision tolerance
        assertTrue(Math.abs(currentMillis - convertedBack) < 1000);
    }

    @Test
    @DisplayName("Given DateUtils class, When constructor is invoked, Then instance is created for coverage")
    void testConstructor() {
        // Given & When
        DateUtils instance = new DateUtils();

        // Then
        assertNotNull(instance);
    }
}
