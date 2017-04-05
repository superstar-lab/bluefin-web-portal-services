package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.RolePermission;

public interface RolePermissionDAO {
	RolePermission findByRolePermissionId(long rolePermissionId);

	List<RolePermission> findByRoleId(long roleId);

	long saveRolePermission(RolePermission rolePermission);
}
