package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
@Transactional
public class RoleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRoleDAO userRoleDAO;
	
	@Autowired
	private PermissionDAO permissionDAO;

	public List<Role> getRoles() {
		return roleDAO.findAll();
	}

	/**
	 * Get roles depending on the user and privileges of the user
	 * 
	 * @param authentication
	 * @return list of roles
	 */
	public List<Role> getRoles(Authentication authentication) {
		LOGGER.info("Entering to get Roles");
		User user = userDAO.findByUsername(authentication.getName());

		if (user == null) {
			LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<>(0);
		}

		List<Role> roleList;
		/**
		 * this condition check only for role ADMINISTRATIVE 
		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			roleList = roleDAO.findAll();
			
		}*/

		//this condition check for role ADMINISTRATIVE as well as MANAGE_ALL_USERS
		if (userService.hasPermissionToManageAllUsers(authentication)) {
			roleList = roleDAO.findAll();
		}

		// Roles that belongs to a user
		else {
			List<Role> list = new ArrayList<>();
			for (UserRole userRole : userRoleDAO.findByUserId(user.getUserId())) {
				long roleId = userRole.getRoleId();
				list.add(roleDAO.findByRoleId(roleId));
			}
			List<Long> rolesFromUser = list.stream().map(userRole -> userRole.getRoleId()).collect(Collectors.toList());
			LOGGER.info("Exiting from get Roles");
			roleList = roleDAO.findAll(rolesFromUser);
		}

		for (Role role : roleList) {
			List<Permission> permissionList = permissionDAO.findByRoleId(role.getRoleId());
			role.setPermissions(permissionList);
		}
		
		return roleList;
	}

	/**
	 * Get all role objects by the entered ids
	 * 
	 * @param rolesIds
	 *            list of role ids that we need to find
	 * @return list of roles
	 * @throws CustomBadRequestException
	 *             when at least one id does not exist
	 */
	public List<Role> getRolesByIds(Set<Long> rolesIds) {
		List<Long> list = new ArrayList<>(rolesIds);
		List<Role> result = roleDAO.findAll(list);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(" Role result size :{} ",result.size());
		}
		if (result != null && result.size() == rolesIds.size()) {
			return result;
		}

		// Create a detail error
		if (result == null || result.isEmpty()) {
			throw new CustomBadRequestException("The following roles don't exist.  List = " + rolesIds);
		}

		Set<Long> rolesNotFound = rolesIds.stream()
				.filter(x -> !result.stream().map(Role::getRoleId).collect(Collectors.toSet()).contains(x))
				.collect(Collectors.toSet());

		throw new CustomBadRequestException("The following roles don't exist.  List = " + rolesNotFound);
	}

	public Role getRoleByName(String roleName) {
		LOGGER.info("Fetching RoleByName= {}",roleName);
		return roleDAO.findByRoleName(roleName);
	}

	/**
	 * Get role by id
	 * 
	 * @param id
	 *            of the role
	 * @return Role object
	 * @throws CustomNotFoundException
	 *             when role is not found
	 */
	public Role getRoleById(Long id) {
		Role role = roleDAO.findByRoleId(id);

		LOGGER.info("role :={} ",role);
		if (role == null) {
			throw new CustomNotFoundException(String.format("Unable to find role with id = [%s]", id));
		}

		List<Permission> permissionList = permissionDAO.findByRoleId(role.getRoleId());
		role.setPermissions(permissionList);
		
		return role;
	}
}
