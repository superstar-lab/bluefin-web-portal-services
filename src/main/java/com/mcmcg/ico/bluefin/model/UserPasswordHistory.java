package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserPasswordHistory extends Common implements Serializable {

	private static final long serialVersionUID = -8557780879103604568L;
	
	private Long passwordHistoryID;
	private Long userId;
	@JsonIgnore
	private String previousPassword;
	public Long getPasswordHistoryID() {
		return passwordHistoryID;
	}
	public void setPasswordHistoryID(Long passwordHistoryID) {
		this.passwordHistoryID = passwordHistoryID;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getPreviousPassword() {
		return previousPassword;
	}
	public void setPreviousPassword(String previousPassword) {
		this.previousPassword = previousPassword;
	}
	
	
}
