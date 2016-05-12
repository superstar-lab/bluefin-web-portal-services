package com.mcmcg.ico.bluefin.rest.resource;

import java.util.HashMap;
import java.util.Map;

public enum StatusCode {
    APPROVED(1), DENIED(2), ERROR(3);

    private Integer statusCode;

    private static Map<Integer, StatusCode> map = new HashMap<Integer, StatusCode>();

    static {
        for (StatusCode statusEnum : StatusCode.values()) {
            map.put(statusEnum.statusCode, statusEnum);
        }
    }

    private StatusCode(final Integer code) {
        statusCode = code;
    }

    public static StatusCode valueOf(int code) {
        return map.get(code);
    }

    public static Integer getStatusCodeByString(String code) {
        for (StatusCode e : StatusCode.values()) {
            if (code.equals(e.name()))
                return e.statusCode;
        }
        return null;
    }

}