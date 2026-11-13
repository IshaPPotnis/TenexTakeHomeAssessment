package com.tenextakehome.service.filter;

public interface Filter {

    FilterOperator getOperator();

    boolean execute(Object actualValue, Object expectedValue);
}
