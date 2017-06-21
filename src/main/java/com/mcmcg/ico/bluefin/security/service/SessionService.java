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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.model.UserLoginHistory.MessageCode;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.RolePermissionDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserLegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserLoginHistoryDAO;
import com.mcmcg.ico.bluefin.repository.UserPreferenceDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.TokenResponse;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.ThirdPartyAppResource;
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
	private UserDAO userDAO;
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
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private UserLegalEntityAppDAO userLegalEntityAppDAO;
	@Autowired
	private UserPreferenceDAO userPreferenceDAO;

	public UsernamePasswordAuthenticationToken authenticate(final String username, final String password) {
		LOGGER.info("Entering SessionService :: authenticate()");
		User user = userDAO.findByUsername(username);
		UserLoginHistory userLoginHistory = new UserLoginHistory();
		userLoginHistory.setUsername(username);
		userLoginHistory.setPassword(passwordEncoder.encode(password));

		LOGGER.debug("SessionService :: authenticate() : user is : "+user);
		if (user == null) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_FOUND.getValue());
			throw new CustomUnauthorizedException("Invalid credentials");
		}
		userLoginHistory.setUserId(user.getUserId());
		if ("NEW".equals(user.getStatus())) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account is not activated yet.");
		}
		if ("INACTIVE".equals(user.getStatus())) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account was deactivated.");
		}
		if (!passwordEncoder.matches(password, user.getPassword())) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_PASSWORD_NOT_FOUND.getValue());
			throw new CustomUnauthorizedException("Invalid credentials");
		}

		saveUserLoginHistory(userLoginHistory, MessageCode.SUCCESS.getValue());
		LOGGER.info("Exit from SessionService :: authenticate()");
		return new UsernamePasswordAuthenticationToken(username, password);
	}

	private void saveUserLoginHistory(UserLoginHistory userLoginHistory, Integer messageCode) {
		LOGGER.debug("Entering SessionService :: saveUserLoginHistory() : userLoginHistory value is "+userLoginHistory);
		if (userLoginHistory != null) {
			userLoginHistory.setMessageId(messageCode);
			userLoginHistoryDAO.saveUserLoginHistory(userLoginHistory);
		}
		LOGGER.info("Exit from SessionService :: saveUserLoginHistory()");
	}

	public AuthenticationResponse generateToken(final String username) {
		LOGGER.info("Entering SessionService :: generateToken()");
		User user = userService.getUser(username);
		LOGGER.debug("Entering SessionService :: generateToken() : user is : "+user);
		final String token = generateNewToken(username, TokenType.AUTHENTICATION, null);

		user.setLastLogin(new DateTime());
		LOGGER.debug("Creating login response for user: {}", username);
		return getLoginResponse(user, token);
	}

	public String generateNewToken(final String username, TokenType type, final String url) {
		LOGGER.info("Entering SessionService :: generateNewToken()");
		SecurityUser securityUser = userDetailsService.loadUserByUsername(username);
		LOGGER.debug("Entering SessionService :: generateNewToken() : securityUser is : "+securityUser);
		if (securityUser == null) {
			throw new CustomBadRequestException("Error generating token for user " + username);
		}
		LOGGER.info("Exit from SessionService :: generateNewToken()");
		return tokenUtils.generateToken(securityUser, type, url);
	}

	public AuthenticationResponse refreshToken(final String token) {
		LOGGER.info("Parsing token to get user information");
		final String username = tokenUtils.getUsernameFromToken(token);

		// Find user by username
		User user = userService.getUser(username);

		LOGGER.debug("Trying to refresh token for user: {}", username);
		final String newToken = generateNewToken(username, TokenType.AUTHENTICATION, null);

		LOGGER.debug("Creating response for user: {}", username);
		return getLoginResponse(user, newToken);
	}

	/**
	 * Regenerate 
	 * 1. Application or API token of already registered user
	 * 2. Transaction Token
	 * 
	 */
	public TokenResponse generateToken(final String username, TokenType tokenType) {
		LOGGER.debug("Generat token for user-{} of type-{}", username,tokenType);
		return new TokenResponse(generateNewToken(username, tokenType, null));
	}
	
	
	public void deleteSession(final String token) {
		LOGGER.info("Sending token to blacklist");
		String username = tokenUtils.getUsernameFromToken(token);
		LOGGER.debug("SessionService :: deleteSession() : username "+username);
		if (username == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.info("Sending token to blacklistExit from SessionService :: deleteSession()");
		tokenUtils.sendTokenToBlacklist(token, username);
	}

	public void resetPassword(final String username) {
		LOGGER.info("Entering SessionService :: resetPassword()");
		User user = userService.getUser(username);
		LOGGER.debug("SessionService :: resetPassword() : user is : "+user);
		LOGGER.debug("Reseting password of user: {}", username);
		final String link = "/api/users/" + username + "/password";
		final String token = generateNewToken(username, TokenType.FORGOT_PASSWORD, link);
		String content = "Please use the link below to reset your password: \n\n"
				+ propertyService.getPropertyValue("RESET_PASSWORD_EMAIL_LINK") + "?token=" + token;
		// Send email
		LOGGER.info("Exit SessionService :: resetPassword() : ready to send email");
		emailService.sendEmail(user.getEmail(), RESET_PASSWORD_EMAIL_SUBJECT, content);
	}

	private AuthenticationResponse getLoginResponse(final User user, final String token) {
		LOGGER.info("Entering SessionService :: getLoginResponse()");
		AuthenticationResponse response = new AuthenticationResponse();

		response.setToken(token);
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setUsername(user.getUsername());
		response.setEmail(user.getEmail());
		
		Set<Role> roleSet = new HashSet<>();
		Set<Permission> permissionSet = new HashSet<>();
		LOGGER.debug("SessionService :: getLoginResponse() : user roles are :"+userRoleDAO.findByUserId(user.getUserId()).size());
		for (UserRole userRole : userRoleDAO.findByUserId(user.getUserId())) {
			long roleId = userRole.getRoleId();
			roleSet.add(roleDAO.findByRoleId(roleId));
			LOGGER.debug("SessionService :: getLoginResponse() : user rolePermission are :"+rolePermissionDAO.findByRoleId(roleId).size());
			for (RolePermission rolePermission : rolePermissionDAO.findByRoleId(roleId)) {
				long permissionId = rolePermission.getPermissionId();
				permissionSet.add(permissionDAO.findByPermissionId(permissionId));
			}
		}
		response.setRoles(roleSet);
		response.setPermissions(permissionSet);

		Set<LegalEntityApp> legalEntityAppSet = new HashSet<>();
		LOGGER.debug("SessionService :: getLoginResponse() : user userLegalEntityApp size :"+userLegalEntityAppDAO.findByUserId(user.getUserId()).size());
		for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
			long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
			legalEntityAppSet.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));
		}
		response.setLegalEntityApps(legalEntityAppSet);
		String selectedTimeZone = userPreferenceDAO.getSelectedTimeZone(user.getUserId()); 
		response.setSelectedTimeZone(selectedTimeZone);
		LOGGER.debug("Exit from SessionService :: getLoginResponse() : with response : "+response);
		return response;
	}

	public TokenResponse registerApplication(ThirdPartyAppResource thirdparyApp) {
		LOGGER.info("Entering SessionService :: registerApplication()");
		RegisterUserResource userResource = new RegisterUserResource();
		userResource.setUsername(thirdparyApp.getUsername());
		userResource.setFirstName(thirdparyApp.getUsername());
		userResource.setLastName(thirdparyApp.getUsername());
		userResource.setEmail(thirdparyApp.getEmail());
		
		Collection<Role> rolesToAssign = new ArrayList<>();
		rolesToAssign.add(getRoleThirdParty());

		if (!userService.existUsername(thirdparyApp.getUsername())) {
			LOGGER.info("SessionService :: registerApplication(): user not exist  ready to save");
			User newUser = userResource.toUser(rolesToAssign, new ArrayList<LegalEntityApp>());
			newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
			newUser.setIsActive((short) 1);
			newUser.setStatus("ACTIVE");
			userDAO.saveUser(newUser);
		}
		LOGGER.info("Exit from SessionService :: registerApplication()");
		return new TokenResponse(generateNewToken(thirdparyApp.getUsername(), TokenType.APPLICATION, null));
	}

	public Role getRoleThirdParty() {
		LOGGER.info("Entering SessionService :: getRoleThirdParty()");
		String applicationRoleName = propertyService.getPropertyValue("APPLICATION_ROLE_NAME");
		Role roleThirdParty = roleService.getRoleByName(applicationRoleName);
		LOGGER.debug("SessionService :: getRoleThirdParty() : roleThirdParty is : "+roleThirdParty);
		if (roleThirdParty == null) {
			String applicationPermissionName = propertyService.getPropertyValue("APPLICATION_PERMISSION_NAME");
			Permission permissionThirdParty = permissionDAO.findByPermissionName(applicationPermissionName);
			LOGGER.debug("SessionService :: getRoleThirdParty() : permissionThirdParty is : "+permissionThirdParty);
			if (permissionThirdParty == null) {
				permissionThirdParty = new Permission();
				permissionThirdParty.setPermissionName(applicationPermissionName);
				permissionThirdParty.setDescription(StringUtils.capitalize(applicationPermissionName));
				long permissionId = permissionDAO.savePermission(permissionThirdParty);
				permissionDAO.findByPermissionId(permissionId);
			}
			roleThirdParty = new Role();
			roleThirdParty.setRoleName(applicationRoleName);
			roleThirdParty.setDescription(StringUtils.capitalize(applicationRoleName));
			long roleId = roleDAO.saveRole(roleThirdParty);
			roleThirdParty = roleDAO.findByRoleId(roleId);

			RolePermission rolePermission = new RolePermission();
			rolePermissionDAO.saveRolePermission(rolePermission);
		}
		LOGGER.info("Exit from SessionService :: getRoleThirdParty()");
		return roleThirdParty;
	}

	public boolean sessionHasPermissionToManageAllLegalEntities(Authentication authentication) {
		LOGGER.info("Entering SessionService :: sessionHasPermissionToManageAllLegalEntities()");
		Boolean hasPermission = false;
		LOGGER.debug("SessionService :: sessionHasPermissionToManageAllLegalEntities() : authentication size is :"+(authentication == null ? null : (authentication.getAuthorities() == null ? null : authentication.getAuthorities().size())));
		if (authentication != null) {
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				hasPermission = "ADMINISTRATIVE".equals(authority.getAuthority());
				LOGGER.debug("SessionService :: sessionHasPermissionToManageAllLegalEntities() : hasPermission : "+hasPermission);
				if (hasPermission) {
					break;
				}
			}
		}
		LOGGER.info("Exit from SessionService :: sessionHasPermissionToManageAllLegalEntities()");
		return hasPermission;
	}
	
	public boolean validateToken(final String token) {
		LOGGER.info("Parsing token to get user information");
		final String username = tokenUtils.getUsernameFromToken(token);

		// Find user by username
		User user = userDAO.findByUsername(username);
		LOGGER.debug("Entering SessionService :: validateToken() : user is :"+user);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} 

		LOGGER.info("Exit from SessionService :: validateToken()");
		return true;
	}
}
