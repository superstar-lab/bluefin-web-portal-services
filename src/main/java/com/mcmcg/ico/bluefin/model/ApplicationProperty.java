package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationProperty extends Common {

	@JsonProperty("applicationPropertyId")
	private Long propertyId;
	@JsonProperty("applicationPropertyName")
	private String propertyName;
	@JsonProperty("applicationPropertyValue")
	private String propertyValue;
	@JsonProperty("dataType")
	private String applicationDataType;
	@JsonProperty("description")
	private String applicationDescription;
	@JsonProperty("modifiedBy")
	private String modifiedByUser;

	public ApplicationProperty() {
		// Default Constructor
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getApplicationDataType() {
		return applicationDataType;
	}

	public void setApplicationDataType(String applicationDataType) {
		this.applicationDataType = applicationDataType;
	}

	public String getApplicationDescription() {
		return applicationDescription;
	}

	public void setApplicationDescription(String applicationDescription) {
		this.applicationDescription = applicationDescription;
	}

	public String getModifiedByUser() {
		return modifiedByUser;
	}

	public void setModifiedByUser(String modifiedByUser) {
		this.modifiedByUser = modifiedByUser;
	}
	
	
}
