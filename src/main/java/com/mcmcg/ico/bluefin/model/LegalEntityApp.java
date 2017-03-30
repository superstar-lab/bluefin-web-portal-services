package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LegalEntityApp implements Serializable {

	private static final long serialVersionUID = 3424245887382516199L;

	private Long legalEntityAppId;
	private String legalEntityAppName;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateModified = new DateTime();
	@JsonIgnore
	private String modifiedBy;
	private Short isActive = 1;

	public LegalEntityApp() {
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

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public DateTime getDateModified() {
		return dateModified;
	}

	public void setDateModified(DateTime dateModified) {
		this.dateModified = dateModified;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}
}
