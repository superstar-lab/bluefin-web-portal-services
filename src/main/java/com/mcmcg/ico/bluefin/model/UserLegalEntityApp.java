package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class UserLegalEntityApp extends Common implements Serializable {

	private static final long serialVersionUID = 5215039441336963323L;

	private Long userLegalEntityAppId;
	private Long userId;
	private Long legalEntityAppId;
	private User user;

	public UserLegalEntityApp() {
		// Default Constructor
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	
}
