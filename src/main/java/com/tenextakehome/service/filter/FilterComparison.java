package com.tenextakehome.service.filter;

import java.math.BigDecimal;
import java.util.OptionalInt;

final class FilterComparison {

    private FilterComparison() {
    }

    static OptionalInt compare(Object actualValue, Object expectedValue) {
        if (actualValue == null || expectedValue == null) {
            return OptionalInt.empty();
        }

        if (actualValue instanceof Number actualNumber && expectedValue instanceof Number expectedNumber) {
            try {
                return OptionalInt.of(
                        new BigDecimal(actualNumber.toString()).compareTo(new BigDecimal(expectedNumber.toString())));
            } catch (NumberFormatException exception) {
                return OptionalInt.empty();
            }
        }

        if (actualValue instanceof String actualString && expectedValue instanceof String expectedString) {
            return OptionalInt.of(actualString.compareTo(expectedString));
        }

        return OptionalInt.empty();
    }
}
