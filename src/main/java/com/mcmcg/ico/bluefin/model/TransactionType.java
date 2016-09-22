package com.mcmcg.ico.bluefin.model;

public enum TransactionType {
    SALE("SALE"), VOID("VOID"), REFUND("REFUND"), REMITTANCE("REMITTANCE");

    private final String type;

    private TransactionType(final String value) {
        this.type = value;
    }

    @Override
    public String toString() {
        return type;
    }
}
