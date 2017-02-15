package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RolePermissionDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;

@Service("userDetailsService")
@Transactional
public class CustomUserDetailsService implements UserDetailsService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserRoleDAO userRoleDAO;
	@Autowired
	private PermissionDAO permissionDAO;
	@Autowired
	private RolePermissionDAO rolePermissionDAO;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);

		return user == null ? null : new SecurityUser(user, getAuthorities(userRoleDAO.findByUserId(user.getUserId())));
	}

	private Collection<? extends GrantedAuthority> getAuthorities(Collection<UserRole> roles) {
		return getGrantedAuthorities(getPermissions(roles));
	}

	private List<String> getPermissions(Collection<UserRole> roles) {
		List<String> permissions = new ArrayList<String>();
		List<RolePermission> collection = new ArrayList<RolePermission>();
		for (UserRole userRole : roles) {
			long roleId = userRole.getRoleId();
			collection.addAll(rolePermissionDAO.findByRoleId(roleId));
		}
		for (RolePermission rolePermission : collection) {
			long permissionId = rolePermission.getPermissionId();
			permissions.add(permissionDAO.findByPermissionId(permissionId).getPermissionName());
		}
		return permissions;
	}

	private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		for (String permission : permissions) {
			authorities.add(new SimpleGrantedAuthority(permission));
		}
		return authorities;
	}
}
