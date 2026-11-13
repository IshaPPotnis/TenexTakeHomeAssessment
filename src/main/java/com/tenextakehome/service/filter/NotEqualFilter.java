package com.tenextakehome.service.filter;

public class NotEqualFilter implements Filter {

    private final EqualFilter equalFilter = new EqualFilter();

    @Override
    public FilterOperator getOperator() {
        return FilterOperator.NOT_EQUAL;
    }

    @Override
    public boolean execute(Object actualValue, Object expectedValue) {
        return !equalFilter.execute(actualValue, expectedValue);
    }
}
