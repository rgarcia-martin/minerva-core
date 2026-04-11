package com.fractalmindstudio.minerva_core.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

public final class DomainRules {

    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 4;
    public static final String MUST_NOT_BE_NULL_SUFFIX = " must not be null";
    public static final String MUST_NOT_BE_BLANK_SUFFIX = " must not be blank";
    public static final String MUST_BE_GREATER_OR_EQUAL_TO_ZERO_SUFFIX = " must be greater than or equal to zero";

    private DomainRules() {
    }

    public static <T> T requireNonNull(final T value, final String fieldName) {
        return Objects.requireNonNull(value, fieldName + MUST_NOT_BE_NULL_SUFFIX);
    }

    public static String requireNonBlank(final String value, final String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + MUST_NOT_BE_BLANK_SUFFIX);
        }

        return value.trim();
    }

    public static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }

        final String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    public static String normalizeEmail(final String value, final String fieldName) {
        return requireNonBlank(value, fieldName).toLowerCase(Locale.ROOT);
    }

    public static BigDecimal requirePositiveOrZero(final BigDecimal value, final String fieldName) {
        requireNonNull(value, fieldName);

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + MUST_BE_GREATER_OR_EQUAL_TO_ZERO_SUFFIX);
        }

        return value;
    }

    public static int requirePositiveOrZero(final int value, final String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + MUST_BE_GREATER_OR_EQUAL_TO_ZERO_SUFFIX);
        }

        return value;
    }

    public static BigDecimal scaleMoney(final BigDecimal value) {
        if (value == null) {
            return null;
        }

        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal scaleRate(final BigDecimal value) {
        if (value == null) {
            return null;
        }

        return value.setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }
}
