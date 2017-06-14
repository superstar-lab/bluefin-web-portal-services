package com.mcmcg.ico.bluefin.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.security.CustomAccessDeniedHandler;

@Component
public class SecurityUserFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUserFactory.class);

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
		LOGGER.info("Entering SecurityUserFactory :: getRoles()");
		List<SimpleGrantedAuthority> result = new ArrayList<SimpleGrantedAuthority>();
		if (roles == null) {
			return null;
		} else {
			LOGGER.debug("SecurityUserFactory :: getRoles() - user roles is not null with size : "+roles.size());
			for (UserRole userRole : roles) {
				long roleId = userRole.getRoleId();
				result.add(new SimpleGrantedAuthority(roleDAO.findByRoleId(roleId).getRoleName()));
			}
		}
		LOGGER.info("Exit SecurityUserFactory :: getRoles() with result : ");
		LOGGER.debug("Exit SecurityUserFactory :: getRoles() with result : "+result);
		return result;
	}
}
