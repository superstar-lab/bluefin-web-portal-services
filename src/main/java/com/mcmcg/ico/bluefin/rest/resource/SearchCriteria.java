package com.mcmcg.ico.bluefin.rest.resource;

import lombok.Data;

@Data
public class SearchCriteria {

    private String key;
    private String operation;
    private Object value;

    public SearchCriteria() {
    }

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
}
