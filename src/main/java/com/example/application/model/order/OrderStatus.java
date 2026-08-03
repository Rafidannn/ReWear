package com.example.application.model.order;

public enum OrderStatus {
    MENUNGGU_PEMBAYARAN("menunggu_pembayaran"),
    DIBAYAR("dibayar"),
    DIPROSES("diproses"),
    DIKIRIM("dikirim"),
    DITERIMA("diterima"),
    SELESAI("selesai"),
    KOMPLAIN("komplain"),
    DIBATALKAN("dibatalkan");

    private final String value;

    OrderStatus(String value) {
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
