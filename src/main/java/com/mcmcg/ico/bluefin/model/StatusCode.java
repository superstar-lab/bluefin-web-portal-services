package com.mcmcg.ico.bluefin.model;

public enum StatusCode {
    A("1"), D("2"), E("3");

    private final String code;

    private StatusCode(final String value) {
        this.code = value;
    }

    public String showCode() {
        return code;
    }

    public static String getStatusCode(String code) {
        for (StatusCode m : StatusCode.values()) {
            if (m.showCode().equals(code)) {
                return m.toString();
            }
        }
        return "";
    }
}
