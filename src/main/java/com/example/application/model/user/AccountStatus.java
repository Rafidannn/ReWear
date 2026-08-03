package com.example.application.model.user;

public enum AccountStatus {
    ACTIVE("active"),
    SUSPENDED("suspended"),
    BANNED("banned");

    private final String value;

    AccountStatus(String value) {
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
