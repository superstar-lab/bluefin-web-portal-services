package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserRole extends Common implements Serializable {

	private static final long serialVersionUID = 2576929644562716504L;

	private Long userRoleId;
	private Long userId;
	private Long roleId;
	@JsonIgnore
	private Role role;

	@JsonIgnore
    private User user;

	public UserRole() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof UserRole)) {
			return false;
		}
		UserRole userRole = (UserRole) o;
		return userRoleId == userRole.userRoleId && userId == userRole.userId && roleId == userRole.roleId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(userRoleId, userId, roleId);
	}

	public Long getUserRoleId() {
		return userRoleId;
	}

	public void setUserRoleId(Long userRoleId) {
		this.userRoleId = userRoleId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	
}