package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SecurityTokenBlacklist implements Serializable {

	private static final long serialVersionUID = 8371299620284629918L;

	private Long tokenId;
	private String token;
	private String type;
	private Long userId;
	@JsonIgnore
	private DateTime dateCreated = new DateTime();

	public SecurityTokenBlacklist() {
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof SecurityTokenBlacklist)) {
			return false;
		}
		SecurityTokenBlacklist securityTokenBlacklist = (SecurityTokenBlacklist) o;
		return tokenId == securityTokenBlacklist.tokenId && Objects.equals(token, securityTokenBlacklist.token)
				&& Objects.equals(type, securityTokenBlacklist.type) && userId == securityTokenBlacklist.userId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(tokenId, token, type, userId);
	}

	public Long getTokenId() {
		return tokenId;
	}

	public void setTokenId(Long tokenId) {
		this.tokenId = tokenId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}
}
