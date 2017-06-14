package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.security.service.SessionService;

@Service
@Transactional
public class RoleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleService.class);

	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRoleDAO userRoleDAO;

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
		LOGGER.info("Entering to RoleService :: getRoles()");
		User user = userDAO.findByUsername(authentication.getName());

		if (user == null) {
			LOGGER.warn("User not found, then we need to return an empty list.  Details: username = [{}]",
					authentication.getName());
			return new ArrayList<Role>(0);
		}

		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return roleDAO.findAll();
		}

		// MANAGE_ALL_USERS needs to return all roles
		if (userService.hasPermissionToManageAllUsers(authentication)) {
			return roleDAO.findAll();
		}

		// Roles that belongs to a user
		List<Role> list = new ArrayList<Role>();
		for (UserRole userRole : userRoleDAO.findByUserId(user.getUserId())) {
			long roleId = userRole.getRoleId();
			list.add(roleDAO.findByRoleId(roleId));
		}
		List<Long> rolesFromUser = list.stream().map(userRole -> userRole.getRoleId()).collect(Collectors.toList());
		LOGGER.info("Exiting from RoleService :: getRoles()");
		return roleDAO.findAll(rolesFromUser);
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
		List<Long> list = new ArrayList<Long>(rolesIds);
		List<Role> result = roleDAO.findAll(list);

		LOGGER.debug("Exiting from RoleService :: getRolesByIds() : Role result size : "+result.size());
		if (result.size() == rolesIds.size()) {
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
		LOGGER.info("RoleService :: getRoleByName()");
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

		LOGGER.info("RoleService :: getRoleById() : role : "+role);
		if (role == null) {
			throw new CustomNotFoundException(String.format("Unable to find role with id = [%s]", id));
		}

		return role;
	}
}
