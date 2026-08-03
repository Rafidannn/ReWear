package com.example.application.model.user;

public enum Role {
    BUYER_SELLER("buyer_seller"),
    SUPER_ADMIN("super_admin"),
    MODERATOR("moderator"),
    FINANCE("finance");

    private final String value;

    Role(String value) {
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
