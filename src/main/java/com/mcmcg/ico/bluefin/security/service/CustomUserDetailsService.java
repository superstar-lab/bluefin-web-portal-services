package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RolePermissionDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;

@Service("userDetailsService")
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);
	
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private UserRoleDAO userRoleDAO;
	@Autowired
	private PermissionDAO permissionDAO;
	@Autowired
	private RolePermissionDAO rolePermissionDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		LOGGER.info("Entering CustomUserDetailsService :: loadUserByUsername()");
		User user = userDAO.findByUsername(username);

		return user == null ? null : new SecurityUser(user, getAuthorities(userRoleDAO.findByUserId(user.getUserId())));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<UserRole> roles) {
		LOGGER.info("Entering CustomUserDetailsService :: getAuthorities()");
		return getGrantedAuthorities(getPermissions(roles));
	}

	private List<String> getPermissions(Collection<UserRole> roles) {
		LOGGER.info("Entering CustomUserDetailsService :: getAuthorities()");
		List<String> permissions = new ArrayList<>();
		List<RolePermission> collection = new ArrayList<>();
		LOGGER.debug("Entering CustomUserDetailsService :: getAuthorities() : with role size : "+(roles != null ? roles.size() : 0));
		for (UserRole userRole : roles) {
			long roleId = userRole.getRoleId();
			collection.addAll(rolePermissionDAO.findByRoleId(roleId));
		}
		for (RolePermission rolePermission : collection) {
			long permissionId = rolePermission.getPermissionId();
			permissions.add(permissionDAO.findByPermissionId(permissionId).getPermissionName());
		}
		LOGGER.debug("Exit CustomUserDetailsService :: getAuthorities() with permssion :"+permissions);
		return permissions;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
		LOGGER.info("Entering CustomUserDetailsService :: getGrantedAuthorities()");
		List<GrantedAuthority> authorities = new ArrayList<>();
		LOGGER.debug("Entering CustomUserDetailsService :: getGrantedAuthorities() with permission size : "+(permissions == null ? null :permissions.size()));
		for (String permission : permissions) {
			authorities.add(new SimpleGrantedAuthority(permission));
		}
		LOGGER.info("Entering CustomUserDetailsService :: getGrantedAuthorities() with authorities : "+authorities);
		return authorities;
	}
}
