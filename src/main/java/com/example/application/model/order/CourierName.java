package com.example.application.model.order;

public enum CourierName {
    JNE("jne"),
    SICEPAT("sicepat"),
    POS("pos"),
    GOSEND("gosend"),
    GRABEXPRESS("grabexpress"),
    LAINNYA("lainnya");

    private final String value;

    CourierName(String value) {
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
