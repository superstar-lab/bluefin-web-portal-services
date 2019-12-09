package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class LegalEntityApp extends Common implements Serializable {

	private static final long serialVersionUID = 3424245887382516199L;

	private Long legalEntityAppId;
	private String legalEntityAppName;
		
	private Short isActive ;
	
	private String prNumber;
	
	private Short isActiveForBatchUpload;

	public LegalEntityApp() {
		// Default constructor
	}

	public LegalEntityApp(long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LegalEntityApp)) {
			return false;
		}
		LegalEntityApp legalEntityApp = (LegalEntityApp) o;
		return legalEntityAppId == legalEntityApp.legalEntityAppId
				&& Objects.equals(legalEntityAppName, legalEntityApp.legalEntityAppName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(legalEntityAppId, legalEntityAppName);
	}

	public Long getLegalEntityAppId() {
		return legalEntityAppId;
	}

	public void setLegalEntityAppId(Long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
	}

	public String getLegalEntityAppName() {
		return legalEntityAppName;
	}

	public void setLegalEntityAppName(String legalEntityAppName) {
		this.legalEntityAppName = legalEntityAppName;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}

	public String getPrNumber() {
		return prNumber;
	}

	public void setPrNumber(String prNumber) {
		this.prNumber = prNumber;
	}

	public Short getIsActiveForBatchUpload() {
		return isActiveForBatchUpload;
	}

	public void setIsActiveForBatchUpload(Short isActiveForBatchUpload) {
		this.isActiveForBatchUpload = isActiveForBatchUpload;
	}
	
	
}
