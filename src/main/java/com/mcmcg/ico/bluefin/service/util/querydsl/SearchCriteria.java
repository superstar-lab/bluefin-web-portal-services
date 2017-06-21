package com.mcmcg.ico.bluefin.service.util.querydsl;

import lombok.Data;

@Data
class SearchCriteria {

    private String key;
    private String operation;
    private Object value;

    public SearchCriteria() {
    	// Default Constructor
    }

    public SearchCriteria(String key, String operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }
}
