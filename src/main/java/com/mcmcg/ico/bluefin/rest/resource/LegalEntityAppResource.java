package com.mcmcg.ico.bluefin.rest.resource;

import org.hibernate.validator.constraints.NotEmpty;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;

import lombok.Data;

@Data
public class LegalEntityAppResource {
    @NotEmpty(message = "legal entity name must not be empty")
    private String legalEntityAppName;

    public LegalEntityApp toLegalEntityApp() {
        LegalEntityApp legalEntityApp = new LegalEntityApp();
        legalEntityApp.setLegalEntityAppName(legalEntityAppName);
        return legalEntityApp;
    }

    public LegalEntityApp updateLegalEntityApp(LegalEntityApp legalEntityApp) {
        legalEntityApp.setLegalEntityAppName(legalEntityAppName);
        return legalEntityApp;
    }

}
