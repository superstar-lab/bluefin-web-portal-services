package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Property implements Serializable {

	private static final long serialVersionUID = -3090586532808240374L;

	private Long applicationPropertyId;
	private String applicationPropertyName;
	private String applicationPropertyValue;
	@JsonIgnore
	private String dataType;
	private String description;
	@JsonIgnore
	private DateTime dateCreated;
	@JsonIgnore
	private DateTime dateModified;
	@JsonIgnore
	private String modifiedBy;

	public Property() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Property)) {
			return false;
		}
		Property property = (Property) o;
		return applicationPropertyId == property.applicationPropertyId
				&& Objects.equals(applicationPropertyName, property.applicationPropertyName)
				&& Objects.equals(applicationPropertyValue, property.applicationPropertyValue)
				&& Objects.equals(dataType, property.dataType) && Objects.equals(description, property.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(applicationPropertyId, applicationPropertyName, applicationPropertyValue, dataType,
				description);
	}

	public Long getApplicationPropertyId() {
		return applicationPropertyId;
	}

	public void setApplicationPropertyId(Long applicationPropertyId) {
		this.applicationPropertyId = applicationPropertyId;
	}

	public String getApplicationPropertyName() {
		return applicationPropertyName;
	}

	public void setApplicationPropertyName(String applicationPropertyName) {
		this.applicationPropertyName = applicationPropertyName;
	}

	public String getApplicationPropertyValue() {
		return applicationPropertyValue;
	}

	public void setApplicationPropertyValue(String applicationPropertyValue) {
		this.applicationPropertyValue = applicationPropertyValue;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
