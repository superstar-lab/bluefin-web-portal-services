package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;

@Data
public class LegalEntityApp extends Common implements Serializable {

	private static final long serialVersionUID = 3424245887382516199L;

	private Long legalEntityAppId;
	private String legalEntityAppName;
		
	private Short isActive ;
	
	private String prNumber;

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
}
