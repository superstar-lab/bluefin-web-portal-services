package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

public class RolePermission extends Common implements Serializable {

	private static final long serialVersionUID = 353607968407107607L;

	private Long rolePermissionId;
	private Long roleId;
	private Long permissionId;
	
	public RolePermission() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof RolePermission)) {
			return false;
		}
		RolePermission rolePermission = (RolePermission) o;
		return rolePermissionId == rolePermission.rolePermissionId && roleId == rolePermission.roleId
				&& permissionId == rolePermission.permissionId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(rolePermissionId, roleId, permissionId);
	}

	public Long getRolePermissionId() {
		return rolePermissionId;
	}

	public void setRolePermissionId(Long rolePermissionId) {
		this.rolePermissionId = rolePermissionId;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getPermissionId() {
		return permissionId;
	}

	public void setPermissionId(Long permissionId) {
		this.permissionId = permissionId;
	}
	
	
}
