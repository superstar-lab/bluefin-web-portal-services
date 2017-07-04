package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
@Data
public class Permission extends Common implements Serializable {

	private static final long serialVersionUID = -5655934914808632512L;

	private Long permissionId;
	private String permissionName;
	private String description;

	public Permission() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Permission)) {
			return false;
		}
		Permission permission = (Permission) o;
		return permissionId == permission.permissionId && Objects.equals(permissionName, permission.permissionName)
				&& Objects.equals(description, permission.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(permissionId, permissionName, description);
	}
	
}
