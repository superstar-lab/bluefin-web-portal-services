package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.Permission;

public interface PermissionDAO {
	Permission findByPermissionId(long permissionId);

	Permission findByPermissionName(String permissionName);

	long savePermission(Permission permission);
}