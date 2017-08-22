package com.mcmcg.ico.bluefin.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.Data;

@Data
public class Role extends Common implements Serializable {

	private static final long serialVersionUID = -8094368312066129639L;

	private Long roleId;
	private String roleName;
	private String description;
	private List<Permission> permissionSet;

	public Role() {
		// Default Constructor
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Role)) {
			return false;
		}
		Role role = (Role) o;
		return roleId == role.roleId && Objects.equals(roleName, role.roleName)
				&& Objects.equals(description, role.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleId, roleName, description);
	}
	
}
