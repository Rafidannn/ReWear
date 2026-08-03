package com.example.application.model.order;

public enum ShippingMethod {
    COD_SEKOLAH("cod_sekolah"),
    EKSPEDISI("ekspedisi"),
    POS("pos"),
    GOSEND("gosend"),
    GRABEXPRESS("grabexpress"),
    LAINNYA("lainnya");

    private final String value;

    ShippingMethod(String value) {
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
