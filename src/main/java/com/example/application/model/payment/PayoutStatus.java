package com.example.application.model.payment;

public enum PayoutStatus {
    REQUESTED("requested"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    REJECTED("rejected");

    private final String value;

    PayoutStatus(String value) {
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
