package com.mcmcg.ico.bluefin.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;

@Component
public class SecurityUserFactory {

	private static RoleDAO roleDAO;
	private static UserRoleDAO userRoleDAO;

	@Autowired
	public SecurityUserFactory(RoleDAO roleDAO, UserRoleDAO userRoleDAO) {
		SecurityUserFactory.roleDAO = roleDAO;
		SecurityUserFactory.userRoleDAO = userRoleDAO;
	}

	public static SecurityUser create(User user) {
		return new SecurityUser(user, getRoles(userRoleDAO.findByUserId(user.getUserId())));
	}

	public static Collection<? extends GrantedAuthority> getRoles(Collection<UserRole> roles) {
		List<SimpleGrantedAuthority> result = new ArrayList<SimpleGrantedAuthority>();
		if (roles == null) {
			return null;
		} else {
			for (UserRole userRole : roles) {
				long roleId = userRole.getRoleId();
				result.add(new SimpleGrantedAuthority(roleDAO.findByRoleId(roleId).getRoleName()));
			}
		}
		return result;
	}
}
