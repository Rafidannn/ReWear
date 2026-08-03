package com.example.application.model.order;

public enum ReturnStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    ITEM_SHIPPED("item_shipped"),
    COMPLETED("completed");

    private final String value;

    ReturnStatus(String value) {
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
