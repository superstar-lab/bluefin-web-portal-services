package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.model.UserLoginHistory.MessageCode;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.UserLegalEntity;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.RolePermissionDAO;
import com.mcmcg.ico.bluefin.repository.UserLoginHistoryDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.BasicTokenResponse;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.service.EmailService;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.RoleService;
import com.mcmcg.ico.bluefin.service.UserService;

@Service
@Transactional
public class SessionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);
	private static final String RESET_PASSWORD_EMAIL_SUBJECT = "Bluefin web portal: Forgot password email";
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private UserLoginHistoryDAO userLoginHistoryDAO;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private EmailService emailService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private PermissionDAO permissionDAO;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private RolePermissionDAO rolePermissionDAO;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private UserRoleDAO userRoleDAO;

	public UsernamePasswordAuthenticationToken authenticate(final String username, final String password) {
		User user = userRepository.findByUsername(username);
		UserLoginHistory userLoginHistory = new UserLoginHistory();
		userLoginHistory.setUsername(username);
		userLoginHistory.setUserPassword(passwordEncoder.encode(password));

		if (user == null) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_FOUND.getValue());
			throw new CustomUnauthorizedException("Invalid credentials");
		}
		userLoginHistory.setUserId(user.getUserId());
		if (user.getStatus().equals("NEW")) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account is not activated yet.");
		}
		if (user.getStatus().equals("INACTIVE")) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account was deactivated.");
		}
		if (!passwordEncoder.matches(password, user.getUserPassword())) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_PASSWORD_NOT_FOUND.getValue());
			throw new CustomUnauthorizedException("Invalid credentials");
		}

		saveUserLoginHistory(userLoginHistory, MessageCode.SUCCESS.getValue());
		return new UsernamePasswordAuthenticationToken(username, password);
	}

	private void saveUserLoginHistory(UserLoginHistory userLoginHistory, Integer messageCode) {
		if (userLoginHistory != null) {
			userLoginHistory.setMessageId(messageCode);
			userLoginHistoryDAO.saveUserLoginHistory(userLoginHistory);
		}

	}

	public AuthenticationResponse generateToken(final String username) {
		User user = userService.getUser(username);

		final String token = generateNewToken(username, TokenType.AUTHENTICATION, null);

		user.setLastLogin(new DateTime());
		userRepository.save(user);

		LOGGER.info("Creating login response for user: {}", username);
		return getLoginResponse(user, token);
	}

	public String generateNewToken(final String username, TokenType type, final String url) {
		SecurityUser securityUser = userDetailsService.loadUserByUsername(username);
		if (securityUser == null) {
			throw new CustomBadRequestException("Error generating token for user " + username);
		}

		return tokenUtils.generateToken(securityUser, type, url);
	}

	public AuthenticationResponse refreshToken(final String token) {
		LOGGER.info("Parsing token to get user information");
		final String username = tokenUtils.getUsernameFromToken(token);

		// Find user by username
		User user = userService.getUser(username);

		LOGGER.info("Trying to refresh token for user: {}", username);
		final String newToken = generateNewToken(username, TokenType.AUTHENTICATION, null);

		LOGGER.info("Creating response for user: {}", username);
		return getLoginResponse(user, newToken);
	}

	public void deleteSession(final String token) {
		LOGGER.info("Sending token to blacklist");
		String username = tokenUtils.getUsernameFromToken(token);
		if (username == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		tokenUtils.sendTokenToBlacklist(token, username);
	}

	public void resetPassword(final String username) {
		User user = userService.getUser(username);

		LOGGER.info("Reseting password of user: {}", username);
		final String link = "/api/users/" + username + "/password";
		final String token = generateNewToken(username, TokenType.FORGOT_PASSWORD, link);
		String content = "Please use the link below to reset your password: \n\n"
				+ propertyService.getPropertyValue("RESET_PASSWORD_EMAIL_LINK") + "?token=" + token;
		// Send email
		emailService.sendEmail(user.getEmail(), RESET_PASSWORD_EMAIL_SUBJECT, content);
	}

	private AuthenticationResponse getLoginResponse(final User user, final String token) {
		AuthenticationResponse response = new AuthenticationResponse();

		response.setToken(token);
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setUsername(user.getUsername());

		Set<Role> rolesResult = new HashSet<Role>();
		Set<Permission> permissionsResult = new HashSet<Permission>();
		for (UserRole userRole : userRoleDAO.findByUserId(user.getUserId())) {
			long roleId = userRole.getRoleId();
			rolesResult.add(roleDAO.findByRoleId(roleId));
			for (RolePermission rolePermission : rolePermissionDAO.findByRoleId(roleId)) {
				long permissionId = rolePermission.getPermissionId();
				permissionsResult.add(permissionDAO.findByPermissionId(permissionId));
			}
		}
		response.setRoles(rolesResult);
		response.setPermissions(permissionsResult);

		Set<LegalEntityApp> legalEntityAppsResult = new HashSet<LegalEntityApp>();
		for (UserLegalEntity legalEntity : user.getLegalEntities()) {
			legalEntityAppsResult.add(legalEntity.getLegalEntityApp());

		}
		response.setLegalEntityApps(legalEntityAppsResult);

		return response;
	}

	public BasicTokenResponse registerApplication(String username) {
		RegisterUserResource userResource = new RegisterUserResource();
		userResource.setUsername(username);
		userResource.setFirstName(username);
		userResource.setLastName(username);

		Collection<Role> rolesToAssign = new ArrayList<Role>();
		rolesToAssign.add(getRoleThirdParty());

		if (!userService.existUsername(username)) {
			User newUser = userResource.toUser(rolesToAssign, new ArrayList<LegalEntityApp>());
			newUser.setUserPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
			newUser.setIsActive((short) 1);

			userRepository.save(newUser);
		}
		return new BasicTokenResponse(generateNewToken(username, TokenType.APPLICATION, null));
	}

	public Role getRoleThirdParty() {
		String applicationRoleName = propertyService.getPropertyValue("APPLICATION_ROLE_NAME");
		Role roleThirdParty = roleService.getRoleByName(applicationRoleName);
		if (roleThirdParty == null) {
			String applicationPermissionName = propertyService.getPropertyValue("APPLICATION_PERMISSION_NAME");
			Permission permissionThirdParty = permissionDAO.findByPermissionName(applicationPermissionName);
			if (permissionThirdParty == null) {
				permissionThirdParty = new Permission();
				permissionThirdParty.setPermissionName(applicationPermissionName);
				permissionThirdParty.setDescription(StringUtils.capitalize(applicationPermissionName));
				long permissionId = permissionDAO.savePermission(permissionThirdParty);
				permissionThirdParty = permissionDAO.findByPermissionId(permissionId);
			}
			roleThirdParty = new Role();
			roleThirdParty.setRoleName(applicationRoleName);
			roleThirdParty.setDescription(StringUtils.capitalize(applicationRoleName));
			long roleId = roleDAO.saveRole(roleThirdParty);
			roleThirdParty = roleDAO.findByRoleId(roleId);

			RolePermission rolePermission = new RolePermission();
			//rolePermission.setPermission(permissionThirdParty);
			//rolePermission.setRole(roleThirdParty);
			rolePermissionDAO.saveRolePermission(rolePermission);
		}
		return roleThirdParty;
	}

	public boolean sessionHasPermissionToManageAllLegalEntities(Authentication authentication) {
		Boolean hasPermission = false;
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			hasPermission = authority.getAuthority().equals("ADMINISTRATIVE");
			if (hasPermission) {
				break;
			}
		}
		return hasPermission;
	}

}
