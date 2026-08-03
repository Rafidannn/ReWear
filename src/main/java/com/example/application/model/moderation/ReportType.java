package com.example.application.model.moderation;

public enum ReportType {
    ORDER_COMPLAINT("order_complaint"),
    USER_VIOLATION("user_violation"),
    CHAT_ISSUE("chat_issue");

    private final String value;

    ReportType(String value) {
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
