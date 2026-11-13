package com.tenextakehome.service.filter;

import java.math.BigDecimal;

final class FilterComparison {

    private FilterComparison() {
    }

    static int compare(Object actualValue, Object expectedValue) {
        if (actualValue == null || expectedValue == null) {
            throw new IllegalArgumentException("Cannot order null filter values");
        }

        if (actualValue instanceof Number actualNumber && expectedValue instanceof Number expectedNumber) {
            try {
                return new BigDecimal(actualNumber.toString()).compareTo(new BigDecimal(expectedNumber.toString()));
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Filter numbers must be finite numeric values", exception);
            }
        }

        if (actualValue instanceof String actualString && expectedValue instanceof String expectedString) {
            return actualString.compareTo(expectedString);
        }

        throw new IllegalArgumentException(
                "Ordering filters require two numbers or two strings of the same type");
    }
}
