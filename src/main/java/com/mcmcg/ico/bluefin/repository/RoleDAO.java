package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.Role;

public interface RoleDAO {
	List<Role> findAll();

	Role findByRoleId(long roleId);

	Role findByRoleName(String roleName);

	long saveRole(Role role);

	int deleteByRoleName(String roleName);
}
