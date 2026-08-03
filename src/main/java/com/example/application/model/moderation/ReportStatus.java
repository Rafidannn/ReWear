package com.example.application.model.moderation;

public enum ReportStatus {
    PENDING("pending"),
    INVESTIGATING("investigating"),
    RESOLVED("resolved"),
    REJECTED("rejected");

    private final String value;

    ReportStatus(String value) {
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
