package com.mcmcg.ico.bluefin.model;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {
    APPROVED("1"), DENIED("2"), ERROR("3"), MCM_EXCEPTION("4"), DATABASE_ERROR("ME1");

    private String statusCode;

    private static Map<String, StatusCode> map = new HashMap<String, StatusCode>();

    static {
        for (StatusCode statusEnum : StatusCode.values()) {
            map.put(statusEnum.statusCode, statusEnum);
        }
    }

    private StatusCode(final String code) {
        statusCode = code;
    }

    public static String getStatusCodeByString(String code) {
        for (StatusCode e : StatusCode.values()) {
            if (code.equals(e.name()))
                return e.statusCode;
        }
        return null;
    }

}