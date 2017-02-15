package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class LegalEntityApp implements Serializable {

    private static final long serialVersionUID = 8230577740929912582L;

    private Long legalEntityAppId;
    private String legalEntityAppName;
    @JsonIgnore
    private String lastModifiedBy;
    private Short isActive = 1;

    public LegalEntityApp() {
    }

    public LegalEntityApp(Long value) {
        legalEntityAppId = value;
    }
}
