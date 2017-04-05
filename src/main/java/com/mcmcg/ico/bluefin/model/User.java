package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.joda.time.DateTime;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class User implements Serializable {

	private static final long serialVersionUID = -8557780879103606219L;

	private Long userId;
	private String username;
	private String firstName;
	private String lastName;
	private Short isActive = 0;
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime lastLogin = new DateTime();
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateCreated = new DateTime();
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateUpdated = new DateTime();
	private String email;
	@JsonIgnore
	private String password;
	@JsonIgnore
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private DateTime dateModified = new DateTime();
	@JsonIgnore
	@LastModifiedBy
	private String modifiedBy;
	private String status;

	@JsonIgnore
	private Collection<UserRole> roles;

	@JsonIgnore
	private Collection<UserLegalEntityApp> legalEntities;

	public Collection<UserRole> getRoles() {
		return roles;
	}

	public void setRoles(Collection<UserRole> roles) {
		this.roles = roles;
	}

	public Collection<UserLegalEntityApp> getLegalEntities() {
		return legalEntities;
	}

	public void setLegalEntities(Collection<UserLegalEntityApp> legalEntities) {
		this.legalEntities = legalEntities;
	}

	public User() {
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof User)) {
			return false;
		}
		User user = (User) o;
		return userId == user.userId && Objects.equals(username, user.username)
				&& Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName)
				&& isActive == user.isActive && Objects.equals(email, user.email)
				&& Objects.equals(password, user.password) && Objects.equals(status, user.status);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, username, firstName, lastName, isActive, password, status);
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Short getIsActive() {
		return isActive;
	}

	public void setIsActive(Short isActive) {
		this.isActive = isActive;
	}

	public DateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(DateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public DateTime getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(DateTime dateCreated) {
		this.dateCreated = dateCreated;
	}

	public DateTime getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(DateTime dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void addRole(Role role) {
		if (roles == null) {
			roles = new ArrayList<UserRole>();
		}

		UserRole userRole = new UserRole();
		userRole.setRole(role);
		userRole.setUser(this);
		roles.add(userRole);
	}

	public void addLegalEntityApp(LegalEntityApp legalEntityApp) {
		if (legalEntities == null) {
			legalEntities = new ArrayList<UserLegalEntityApp>();
		}

		UserLegalEntityApp userLE = new UserLegalEntityApp();
		userLE.setLegalEntityAppId(legalEntityApp.getLegalEntityAppId());
		userLE.setUser(this);
		legalEntities.add(userLE);
	}

}