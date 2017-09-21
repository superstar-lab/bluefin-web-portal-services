package com.mcmcg.ico.bluefin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
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
	@JsonProperty("requestStatus")
	private String requestStatus;

	public ApplicationProperty() {
		// Default Constructor
	}
}
