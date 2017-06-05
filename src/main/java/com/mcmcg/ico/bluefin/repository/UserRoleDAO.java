package com.mcmcg.ico.bluefin.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mcmcg.ico.bluefin.model.UserRole;

public interface UserRoleDAO {
	void saveRoles(Collection<UserRole> roles);

	void deleteUserRoleById(Set<Long> rolesToRemove);
	
	List<UserRole> findByUserId(long userId);
}
