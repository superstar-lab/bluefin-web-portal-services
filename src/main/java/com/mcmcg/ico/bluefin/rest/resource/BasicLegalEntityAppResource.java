package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;

import lombok.Data;

@Data
public class BasicLegalEntityAppResource implements Serializable {
    private static final long serialVersionUID = 8649893596541033808L;

    @NotBlank(message = "Legal entity name cannot be empty or null")
    @Pattern(regexp = "^[\\w-\\'\\s\\.]*$", message = "Legal entity name must be alphanumeric")
    private String legalEntityAppName;

    public LegalEntityApp toLegalEntityApp() {
        LegalEntityApp legalEntityApp = new LegalEntityApp();
        legalEntityApp.setLegalEntityAppName(legalEntityAppName);
        return legalEntityApp;
    }
}
