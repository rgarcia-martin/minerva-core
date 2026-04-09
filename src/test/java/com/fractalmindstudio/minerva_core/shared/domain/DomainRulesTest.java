package com.fractalmindstudio.minerva_core.shared.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the shared validation and scaling utilities used across all domain models.
 * Every invariant enforced by DomainRules is exercised here so that domain records
 * can rely on these guarantees without duplicating validation tests.
 */
class DomainRulesTest {

    private static final String FIELD_NAME = "testField";

    // --- requireNonNull ---

    @Test
    void requireNonNull_shouldReturnValueWhenPresent() {
        final String value = "hello";

        final String result = DomainRules.requireNonNull(value, FIELD_NAME);

        assertEquals(value, result);
    }

    @Test
    void requireNonNull_shouldThrowWhenNull() {
        final NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> DomainRules.requireNonNull(null, FIELD_NAME)
        );

        assertEquals(FIELD_NAME + DomainRules.MUST_NOT_BE_NULL_SUFFIX, exception.getMessage());
    }

    // --- requireNonBlank ---

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void requireNonBlank_shouldThrowWhenNullEmptyOrWhitespace(final String value) {
        assertThrows(
                IllegalArgumentException.class,
                () -> DomainRules.requireNonBlank(value, FIELD_NAME)
        );
    }

    @Test
    void requireNonBlank_shouldTrimAndReturnValue() {
        final String result = DomainRules.requireNonBlank("  hello  ", FIELD_NAME);

        assertEquals("hello", result);
    }

    @Test
    void requireNonBlank_shouldIncludeFieldNameInMessage() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DomainRules.requireNonBlank("", FIELD_NAME)
        );

        assertEquals(FIELD_NAME + DomainRules.MUST_NOT_BE_BLANK_SUFFIX, exception.getMessage());
    }

    // --- requirePositiveOrZero (BigDecimal) ---

    @Test
    void requirePositiveOrZero_bigDecimal_shouldAcceptZero() {
        final BigDecimal result = DomainRules.requirePositiveOrZero(BigDecimal.ZERO, FIELD_NAME);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void requirePositiveOrZero_bigDecimal_shouldAcceptPositive() {
        final BigDecimal value = new BigDecimal("99.99");

        final BigDecimal result = DomainRules.requirePositiveOrZero(value, FIELD_NAME);

        assertEquals(value, result);
    }

    @Test
    void requirePositiveOrZero_bigDecimal_shouldThrowWhenNegative() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DomainRules.requirePositiveOrZero(new BigDecimal("-0.01"), FIELD_NAME)
        );
    }

    @Test
    void requirePositiveOrZero_bigDecimal_shouldThrowWhenNull() {
        assertThrows(
                NullPointerException.class,
                () -> DomainRules.requirePositiveOrZero((BigDecimal) null, FIELD_NAME)
        );
    }

    // --- requirePositiveOrZero (int) ---

    @Test
    void requirePositiveOrZero_int_shouldAcceptZero() {
        assertEquals(0, DomainRules.requirePositiveOrZero(0, FIELD_NAME));
    }

    @Test
    void requirePositiveOrZero_int_shouldAcceptPositive() {
        assertEquals(5, DomainRules.requirePositiveOrZero(5, FIELD_NAME));
    }

    @Test
    void requirePositiveOrZero_int_shouldThrowWhenNegative() {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DomainRules.requirePositiveOrZero(-1, FIELD_NAME)
        );

        assertEquals(FIELD_NAME + DomainRules.MUST_BE_GREATER_OR_EQUAL_TO_ZERO_SUFFIX, exception.getMessage());
    }

    // --- scaleMoney ---

    @Test
    void scaleMoney_shouldScaleToTwoDecimalPlaces() {
        final BigDecimal result = DomainRules.scaleMoney(new BigDecimal("10.12345"));

        assertEquals(new BigDecimal("10.12"), result);
    }

    @Test
    void scaleMoney_shouldRoundHalfUp() {
        final BigDecimal result = DomainRules.scaleMoney(new BigDecimal("10.125"));

        assertEquals(new BigDecimal("10.13"), result);
    }

    @Test
    void scaleMoney_shouldReturnNullWhenNull() {
        assertNull(DomainRules.scaleMoney(null));
    }

    // --- scaleRate ---

    @Test
    void scaleRate_shouldScaleToFourDecimalPlaces() {
        final BigDecimal result = DomainRules.scaleRate(new BigDecimal("21.456789"));

        assertEquals(new BigDecimal("21.4568"), result);
    }

    @Test
    void scaleRate_shouldRoundHalfUp() {
        final BigDecimal result = DomainRules.scaleRate(new BigDecimal("21.45675"));

        assertEquals(new BigDecimal("21.4568"), result);
    }

    @Test
    void scaleRate_shouldReturnNullWhenNull() {
        assertNull(DomainRules.scaleRate(null));
    }
}
