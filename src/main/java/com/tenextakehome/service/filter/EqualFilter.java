package com.tenextakehome.service.filter;

import java.math.BigDecimal;
import java.util.Objects;

public class EqualFilter implements Filter {

    @Override
    public FilterOperator getOperator() {
        return FilterOperator.EQUAL;
    }

    @Override
    public boolean execute(Object actualValue, Object expectedValue) {
        if (actualValue instanceof Number actualNumber && expectedValue instanceof Number expectedNumber) {
            try {
                return new BigDecimal(actualNumber.toString()).compareTo(new BigDecimal(expectedNumber.toString())) == 0;
            } catch (NumberFormatException exception) {
                return Objects.equals(actualValue, expectedValue);
            }
        }
        return Objects.equals(actualValue, expectedValue);
    }
}
