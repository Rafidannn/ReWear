package com.example.application.model.payment;

public enum TransactionStatus {
    PENDING("pending"),
    SETTLEMENT("settlement"),
    CAPTURE("capture"),
    DENY("deny"),
    CANCEL("cancel"),
    EXPIRE("expire"),
    FAILURE("failure");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
