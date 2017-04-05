package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserLegalEntityApp implements Serializable {

	private static final long serialVersionUID = 5215039441336963323L;

	private Long userLegalEntityAppId;
	private Long userId;
	private Long legalEntityAppId;
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
	
	private User user;

	public UserLegalEntityApp() {
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof UserLegalEntityApp)) {
			return false;
		}
		UserLegalEntityApp userLegalEntityApp = (UserLegalEntityApp) o;
		return userLegalEntityAppId == userLegalEntityApp.userLegalEntityAppId && userId == userLegalEntityApp.userId
				&& legalEntityAppId == userLegalEntityApp.legalEntityAppId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userLegalEntityAppId, userId, legalEntityAppId);
	}

	public Long getUserLegalEntityAppId() {
		return userLegalEntityAppId;
	}

	public void setUserLegalEntityAppId(Long userLegalEntityAppId) {
		this.userLegalEntityAppId = userLegalEntityAppId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getLegalEntityAppId() {
		return legalEntityAppId;
	}

	public void setLegalEntityAppId(Long legalEntityAppId) {
		this.legalEntityAppId = legalEntityAppId;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
