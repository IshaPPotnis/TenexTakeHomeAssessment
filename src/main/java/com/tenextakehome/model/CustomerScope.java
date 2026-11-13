package com.tenextakehome.model;

public final class CustomerScope {

    public static final String GLOBAL = "GLOBAL";

    private CustomerScope() {
    }

    public static boolean isGlobal(String customerName) {
        return customerName == null || customerName.isBlank() || GLOBAL.equals(customerName);
    }

    public static boolean isVisibleTo(String owner, String customerName) {
        return isGlobal(owner) || owner.equals(customerName);
    }
}
