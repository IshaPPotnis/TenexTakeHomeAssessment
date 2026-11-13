package com.tenextakehome.service.filter;

public class GreaterThanFilter implements Filter {

    @Override
    public FilterOperator getOperator() {
        return FilterOperator.GREATER_THAN;
    }

    @Override
    public boolean execute(Object actualValue, Object expectedValue) {
        return FilterComparison.compare(actualValue, expectedValue)
                .stream()
                .anyMatch(comparison -> comparison > 0);
    }
}
