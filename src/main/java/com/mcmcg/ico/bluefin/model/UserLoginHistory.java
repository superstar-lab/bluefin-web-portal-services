package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserLoginHistory implements Serializable {

	private static final long serialVersionUID = -2022911364184731333L;

	public static enum MessageCode {
		SUCCESS(1), ERROR_USER_NOT_FOUND(2), ERROR_PASSWORD_NOT_FOUND(3), ERROR_USER_NOT_ACTIVE(4);

		private final Integer messageCode;

		private MessageCode(Integer messageCode) {
			this.messageCode = messageCode;
		}

		public Integer getValue() {
			return this.messageCode;
		}
	}

	private Long userLoginHistoryId;
	private Long userId;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime loginDateTime = new DateTime();
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	private Integer messageId;
	private String username;
	@JsonIgnore
	private String userPassword;

	public UserLoginHistory() {
	}

	public Long getUserLoginHistoryId() {
		return userLoginHistoryId;
	}

	public void setUserLoginHistoryId(Long userLoginHistoryId) {
		this.userLoginHistoryId = userLoginHistoryId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public DateTime getLoginDateTime() {
		return loginDateTime;
	}

	public void setLoginDateTime(DateTime loginDateTime) {
		this.loginDateTime = loginDateTime;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
}
