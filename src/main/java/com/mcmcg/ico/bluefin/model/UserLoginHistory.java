package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserLoginHistory implements Serializable {

	private static final long serialVersionUID = -2022911364184731333L;

	public enum MessageCode {
		SUCCESS(1), ERROR_USER_NOT_FOUND(2), ERROR_PASSWORD_NOT_FOUND(3), ERROR_USER_NOT_ACTIVE(4), ERROR_USER_IS_LOCKED(5);

		private final Integer messageCodeVal;

		private MessageCode(Integer messageCode) {
			this.messageCodeVal = messageCode;
		}

		public Integer getValue() {
			return this.messageCodeVal;
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
	private String password;

	public UserLoginHistory() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof UserLoginHistory)) {
			return false;
		}
		UserLoginHistory userLoginHistory = (UserLoginHistory) o;
		boolean loginHistIdAndUserIdEq = userLoginHistoryId == userLoginHistory.userLoginHistoryId && userId == userLoginHistory.userId;
		boolean msgIdAndUserNmEq = messageId == userLoginHistory.messageId && Objects.equals(username, userLoginHistory.username);
		return loginHistIdAndUserIdEq
				&& msgIdAndUserNmEq
				&& Objects.equals(password, userLoginHistory.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userLoginHistoryId, userId, messageId, username, password);
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
