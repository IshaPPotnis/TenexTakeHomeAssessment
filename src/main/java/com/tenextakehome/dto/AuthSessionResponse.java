package com.tenextakehome.dto;

public class AuthSessionResponse {

    private String customerName;
    private String accessToken;

    public AuthSessionResponse() {
    }

    public AuthSessionResponse(String customerName) {
        this(customerName, null);
    }

    public AuthSessionResponse(String customerName, String accessToken) {
        this.customerName = customerName;
        this.accessToken = accessToken;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
