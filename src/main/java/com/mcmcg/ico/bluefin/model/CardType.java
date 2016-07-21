package com.mcmcg.ico.bluefin.model;

public enum CardType {
    DEBIT("DEBIT"), CREDIT("CREDIT");

    private final String type;

    private CardType(final String value) {
        this.type = value;
    }

    @Override
    public String toString() {
        return type;
    }
}
