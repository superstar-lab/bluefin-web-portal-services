package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.UserRole;

public interface UserRoleDAO {
	List<UserRole> findByUserId(long userId);

	List<UserRole> findByRoleId(long roleId);
}
