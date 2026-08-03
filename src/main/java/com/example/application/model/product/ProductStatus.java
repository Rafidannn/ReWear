package com.example.application.model.product;

public enum ProductStatus {
    ACTIVE("active"),
    SOLD_OUT("sold_out"),
    INACTIVE("inactive"),
    REMOVED("removed");

    private final String value;

    ProductStatus(String value) {
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
