package com.mcmcg.ico.bluefin.rest.resource;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;

public class BasicLegalEntityAppResource implements Serializable {
    private static final long serialVersionUID = 8649893596541033808L;

    @NotBlank(message = "Please provide a legal entity name")
    @Pattern(regexp = "^\\w+(\\s|\\.|\\'|-|\\w)*$", message = "Field legal entity name must be alphanumeric")
    private String legalEntityAppName;
    
    private String prNumber;
    private Short isActive ;
    private Short isActiveForBatchUpload;

    public LegalEntityApp toLegalEntityApp() {
        LegalEntityApp legalEntityApp = new LegalEntityApp();
        legalEntityApp.setLegalEntityAppName(legalEntityAppName);
        legalEntityApp.setIsActive(isActive);
        legalEntityApp.setPrNumber(prNumber);
        legalEntityApp.setIsActiveForBatchUpload(isActiveForBatchUpload);
        return legalEntityApp;
    }

	public String getLegalEntityAppName() {
		return legalEntityAppName;
	}

	public void setLegalEntityAppName(String legalEntityAppName) {
		this.legalEntityAppName = legalEntityAppName;
	}

	public String getPrNumber() {
		return prNumber;
	}

	public void setPrNumber(String prNumber) {
		this.prNumber = prNumber;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}

	public Short getIsActiveForBatchUpload() {
		return isActiveForBatchUpload;
	}

	public void setIsActiveForBatchUpload(Short isActiveForBatchUpload) {
		this.isActiveForBatchUpload = isActiveForBatchUpload;
	}
    
    
}
