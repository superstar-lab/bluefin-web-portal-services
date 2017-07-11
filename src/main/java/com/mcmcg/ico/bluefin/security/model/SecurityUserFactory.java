package com.mcmcg.ico.bluefin.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;

@Component
public class SecurityUserFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUserFactory.class);
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private UserRoleDAO userRoleDAO;

	public SecurityUser create(User user) {
		return new SecurityUser(user, getRoles(userRoleDAO.findByUserId(user.getUserId())));
	}

	public Collection<SimpleGrantedAuthority> getRoles(Collection<UserRole> roles) {
		LOGGER.info("Entering to get Roles");
		List<SimpleGrantedAuthority> result = new ArrayList<>();
		if (roles == null) {
			return new ArrayList<>();
		} else {
			LOGGER.debug("user roles is not null with size ={} ",roles.size());
			for (UserRole userRole : roles) {
				long roleId = userRole.getRoleId();
				result.add(new SimpleGrantedAuthority(roleDAO.findByRoleId(roleId).getRoleName()));
			}
		}
		LOGGER.info("Exit with result : ");
		LOGGER.debug("Exit SecurityUserFactory getRoles() with result ={} ",result);
		return result;
	}
}
