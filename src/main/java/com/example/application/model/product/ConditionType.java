package com.example.application.model.product;

public enum ConditionType {
    BARU("baru"),
    BEKAS("bekas");

    private final String value;

    ConditionType(String value) {
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
