package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.enums.UserStatus;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Permission;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.RolePermission;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserLoginHistory;
import com.mcmcg.ico.bluefin.model.UserPasswordHistory;
import com.mcmcg.ico.bluefin.model.UserLoginHistory.MessageCode;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.PermissionDAO;
import com.mcmcg.ico.bluefin.repository.RoleDAO;
import com.mcmcg.ico.bluefin.repository.RolePermissionDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserLoginHistoryDAO;
import com.mcmcg.ico.bluefin.repository.UserPreferenceDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.ApplicationGenericException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.ThirdPartyAppResource;
import com.mcmcg.ico.bluefin.rest.resource.TokenResponse;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.service.EmailService;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.RoleService;
import com.mcmcg.ico.bluefin.service.UserService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

@Service
@Transactional
public class SessionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionService.class);
	
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
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private UserPreferenceDAO userPreferenceDAO;

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public UsernamePasswordAuthenticationToken authenticate(final String username, final String password) {
		LOGGER.info("Entering to authenticate");
		User user = userDAO.findByUsername(username);
		UserLoginHistory userLoginHistory = new UserLoginHistory();
		userLoginHistory.setUsername(username);
		userLoginHistory.setPassword(passwordEncoder.encode(password));
		Integer wrongPasswordCounterNextVal = 0;
		String wrongPasswordMaxLimit;
		int wrongPasswordMaxLimitVal;
		String accountLockedDurationSecs;
		int accountLockedDurationSecsVal;
		DateTime currentTimeUTC = new DateTime(DateTimeZone.UTC);

		LOGGER.debug("user is ={} ",user);
		if (user == null) {
			
			LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", username, BluefinWebPortalConstants.SEPARATOR,
					"Reason : User NOT FOUND"));
			
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_FOUND.getValue());
			throw new CustomUnauthorizedException("Invalid credentials");
		}
		userLoginHistory.setUserId(user.getUserId());
		if ("NEW".equals(user.getStatus())) {
			
			LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
					"Reason : User is ", UserStatus.NEW.getStatus()));
			
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account is not activated yet.");
		}
		if ("INACTIVE".equals(user.getStatus())) {
			
			LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
					"Reason : User is ", UserStatus.INACTIVE.getStatus()));
			
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_NOT_ACTIVE.getValue());
			throw new AccessDeniedException("Account was deactivated.");
		}
		if (UserStatus.LOCKED.getStatus().equalsIgnoreCase(user.getStatus())) {
			accountLockedDurationSecs = propertyService.getPropertyValue(BluefinWebPortalConstants.ACCOUNTLOCKEDDURATIONSECONDS);
			try {
				accountLockedDurationSecsVal = accountLockedDurationSecs == null || "".equals(accountLockedDurationSecs.trim()) ? 
						BluefinWebPortalConstants.ACCOUNTLOCKEDDURATIONSECONDSDEFAULT : Integer.parseInt(accountLockedDurationSecs);
				if(accountLockedDurationSecsVal < 0)
					accountLockedDurationSecsVal = BluefinWebPortalConstants.ACCOUNTLOCKEDDURATIONSECONDSDEFAULT;
			} catch(NumberFormatException e) {
				accountLockedDurationSecsVal = BluefinWebPortalConstants.ACCOUNTLOCKEDDURATIONSECONDSDEFAULT;
			}
			if(user.getAccountLockedOn() == null || currentTimeUTC.isEqual(user.getAccountLockedOn().plusSeconds(accountLockedDurationSecsVal)) || 
					currentTimeUTC.isAfter(user.getAccountLockedOn().plusSeconds(accountLockedDurationSecsVal))) {
				user.setStatus(UserStatus.ACTIVE.getStatus());
				user.setAccountLockedOn(null);
			} else {
				LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
						"Reason : User is ", UserStatus.LOCKED.getStatus()));
				
				saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_IS_LOCKED.getValue());
				throw new AccessDeniedException("Account is Locked.");
			}
		}
		if (!passwordEncoder.matches(password, user.getPassword())) {
			
			wrongPasswordCounterNextVal = user.getWrongPasswordCounter() + 1;
			wrongPasswordMaxLimit = propertyService.getPropertyValue(BluefinWebPortalConstants.WRONGPASSWORDMAXLIMIT);
			try {
				wrongPasswordMaxLimitVal = wrongPasswordMaxLimit == null || "".equals(wrongPasswordMaxLimit.trim()) ? 
						BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT : Integer.parseInt(wrongPasswordMaxLimit);
				if(wrongPasswordMaxLimitVal < 0)
					wrongPasswordMaxLimitVal = BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT;
			} catch(NumberFormatException e) {
				wrongPasswordMaxLimitVal = BluefinWebPortalConstants.WRONGPASSWORDMAXLIMITDEFAULT;
			}
			
			if (wrongPasswordCounterNextVal >= wrongPasswordMaxLimitVal) {
				user.setStatus(UserStatus.LOCKED.getStatus());
				user.setAccountLockedOn(currentTimeUTC);
				
				LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
						"Reason : ", "PASSWORD IS INVALID", BluefinWebPortalConstants.SEPARATOR,
						"WRONG PASSWORD ATTEMPTS : ", String.valueOf(wrongPasswordCounterNextVal), BluefinWebPortalConstants.SEPARATOR,
						"User is LOCKED"));
				
			} else {
				LOGGER.error(LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
						"Reason : ", "PASSWORD IS INVALID", BluefinWebPortalConstants.SEPARATOR,
						"WRONG PASSWORD ATTEMPTS : ", String.valueOf(wrongPasswordCounterNextVal)));
			}
			
			user.setWrongPasswordCounter(wrongPasswordCounterNextVal);
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_PASSWORD_NOT_FOUND.getValue());
			updateUserLookUp(user);
			throw new CustomUnauthorizedException("Invalid credentials");
		}
		
		if(isPasswordExpire(user, null)) {
			saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_PASSWORD_EXPIRED.getValue());
		}
		else {
			user.setWrongPasswordCounter(wrongPasswordCounterNextVal);
			user.setLastLogin(currentTimeUTC);
			updateUserLookUp(user);
			saveUserLoginHistory(userLoginHistory, MessageCode.SUCCESS.getValue());
		}
		
		LOGGER.info("Exit from authenticate");
		return new UsernamePasswordAuthenticationToken(username, password);
	}

	private void saveUserLoginHistory(UserLoginHistory userLoginHistory, Integer messageCode) {
		LOGGER.debug("userLoginHistory value is ={} ",userLoginHistory);
		if (userLoginHistory != null) {
			userLoginHistory.setMessageId(messageCode);
			userLoginHistoryDAO.saveUserLoginHistory(userLoginHistory);
		}
		LOGGER.info("Exit from saveUserLoginHistory");
	}

	public AuthenticationResponse generateToken(final String username) {
		LOGGER.info("Entering to generate Token");
		User user = userService.getUser(username);
		LOGGER.debug("Entering user is ={} ",user);
		final String token = generateNewToken(username, TokenType.AUTHENTICATION, null);

		user.setLastLogin(new DateTime());
		LOGGER.debug("Creating login response for user: {}", username);
		return getLoginResponse(user, token);
	}

	public String generateNewToken(final String username, TokenType type, final String url) {
		LOGGER.info("Entering to generate New Token");
		SecurityUser securityUser = userDetailsService.loadUserByUsername(username);
		LOGGER.debug("securityUser is ={} ",securityUser);
		if (securityUser == null) {
			throw new CustomBadRequestException("Error generating token for user " + username);
		}
		LOGGER.info("Exit from generate New Token");
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
		LOGGER.debug("username ={} ",username);
		if (username == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.info("Sending token to blacklistExit");
		tokenUtils.sendTokenToBlacklist(token, username);
	}

	public void resetPassword(final String username) {
		LOGGER.info("Entering to reset Password");
		User user = userService.getUser(username);
		LOGGER.debug("user is:- ={} ",user);
		LOGGER.debug("Reseting password of user: {}", username);
		final String link = "/api/users/" + username + "/password";
		final String token = generateNewToken(username, TokenType.FORGOT_PASSWORD, link);
		String content = "Please use the link below to reset your password: \n\n"
				+ propertyService.getPropertyValue("RESET_PASSWORD_EMAIL_LINK") + "?token=" + token;
		// Send email
		LOGGER.info("ready to send email");
		emailService.sendEmail(user.getEmail(), "Bluefin web portal: Forgot password email", content);
	}

	private AuthenticationResponse getLoginResponse(final User user, final String token) {
		LOGGER.info("Entering to get Login Response");
		AuthenticationResponse response = new AuthenticationResponse();

		response.setToken(token);
		response.setFirstName(user.getFirstName());
		response.setLastName(user.getLastName());
		response.setUsername(user.getUsername());
		response.setEmail(user.getEmail());
		
		Set<Role> roleSet = new HashSet<>();
		Set<Permission> permissionSet = new HashSet<>();
		Collection<UserRole> userRoles = user.getRoles();
		LOGGER.debug("user roles are ={}", userRoles != null ? userRoles.size() : 0);
		for (UserRole userRole : userRoles) {
			long roleId = userRole.getRoleId();
			roleSet.add(roleDAO.findByRoleId(roleId));
			List<RolePermission> userRolePermissions = rolePermissionDAO.findByRoleId(roleId);
			LOGGER.debug("user rolePermission are ={}", userRolePermissions != null ? userRolePermissions.size() : 0 );
			for (RolePermission rolePermission : userRolePermissions) {
				long permissionId = rolePermission.getPermissionId();
				permissionSet.add(permissionDAO.findByPermissionId(permissionId));
			}
		}
		response.setRoles(roleSet);
		response.setPermissions(permissionSet);

		Set<LegalEntityApp> legalEntityAppSet = new HashSet<>();
		Collection<UserLegalEntityApp> userLegalEntityApps = user.getLegalEntities();
		LOGGER.debug("user userLegalEntityApp size ={}",userLegalEntityApps != null ? userLegalEntityApps.size() : 0);
		for (UserLegalEntityApp userLegalEntityApp : userLegalEntityApps) {
			long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
			legalEntityAppSet.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));
		}
		response.setLegalEntityApps(legalEntityAppSet);
		String selectedTimeZone = userPreferenceDAO.getSelectedTimeZone(user.getUserId()); 
		response.setSelectedTimeZone(selectedTimeZone);
		
		isPasswordExpire(user, response);
		
		LOGGER.debug("Exit with response ={} ",response);
		return response;
	}

	public TokenResponse registerApplication(ThirdPartyAppResource thirdparyApp) {
		LOGGER.info("Entering to register Application");
		RegisterUserResource userResource = new RegisterUserResource();
		userResource.setUsername(thirdparyApp.getUsername());
		userResource.setFirstName(thirdparyApp.getUsername());
		userResource.setLastName(thirdparyApp.getUsername());
		userResource.setEmail(thirdparyApp.getEmail());
		
		Collection<Role> rolesToAssign = getRoleThirdParty();

		if (!userService.existUsername(thirdparyApp.getUsername())) {
			LOGGER.info("user not exist  ready to save");
			User newUser = userResource.toUser(rolesToAssign, new ArrayList<LegalEntityApp>());
			newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
			newUser.setIsActive((short) 1);
			newUser.setStatus("ACTIVE");
			userDAO.saveUser(newUser);
		}
		LOGGER.info("Exit from register Application");
		return new TokenResponse(generateNewToken(thirdparyApp.getUsername(), TokenType.APPLICATION, null));
	}

	public List<Role> getRoleThirdParty() {
		LOGGER.info("Entering get Role Third Party");
		List<Role> roleList = new ArrayList<>();
		String applicationRoleName = propertyService.getPropertyValue("APPLICATION_ROLE_NAME");
		roleList.add(setUserRolePermission(applicationRoleName));
		String applicationThirdPartyApiRoleName = propertyService.getPropertyValue("THIRD_PARTY_ROLE_NAME");
		roleList.add(setUserRolePermission(applicationThirdPartyApiRoleName));
		LOGGER.info("Exit from get Role Third Party");
		
		return roleList;
	}

	public boolean sessionHasPermissionToManageAllLegalEntities(Authentication authentication) {
		LOGGER.info("Entering to session Has Permission To Manage All LegalEntities");
		Boolean hasPermission = false;
		LOGGER.debug("authentication size is ={}", authentication == null ? null : (authentication.getAuthorities() == null ? null : authentication.getAuthorities().size()));
		if (authentication != null) {
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				hasPermission = "ADMINISTRATIVE".equals(authority.getAuthority());
				LOGGER.debug("hasPermission ={} ",hasPermission);
				if (hasPermission) {
					break;
				}
			}
		}
		LOGGER.info("Exit from session Has Permission To Manage All LegalEntities");
		return hasPermission;
	}
	
	public boolean validateToken(final String token) {
		LOGGER.info("Parsing token to get user information");
		final String username = tokenUtils.getUsernameFromToken(token);

		// Find user by username
		User user = userDAO.findByUsername(username);
		LOGGER.debug("user is= {}",user);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s'.", username));
		} 

		LOGGER.info("Exit from validate Token");
		return true;
	}
	
	private void updateUserLookUp(User user) {
		LOGGER.debug("Inside updateUserLookUp method for User Id : "+user.getUserId());
		
		try {	
			userDAO.updateUserLookUp(user);
		} catch (ApplicationGenericException e) {
			LOGGER.error(e.getMessage(),e);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}
		LOGGER.info("Exit from updateUserLookUp");
	}
	
	private Role setUserRolePermission(String applicationRoleName) {
		LOGGER.debug("Inside getUserRolePermission method for propertyName {} ", applicationRoleName);
		
		Role roleThirdParty = roleService.getRoleByName(applicationRoleName);
		LOGGER.debug("roleThirdParty is ={} ",roleThirdParty);
		if (roleThirdParty == null) {
			String applicationPermissionName = propertyService.getPropertyValue("APPLICATION_PERMISSION_NAME");
			Permission permissionThirdParty = permissionDAO.findByPermissionName(applicationPermissionName);
			LOGGER.debug("permissionThirdParty is ={} ",permissionThirdParty);
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
		LOGGER.info("Exit from setUserRolePermission");
		
		return roleThirdParty;
		
	}
	
	public boolean isPasswordExpire(final User user, AuthenticationResponse response) {
		
		boolean isPasswordExpire = false;
		ArrayList<UserPasswordHistory> passwordHistoryList = userService.getPasswordHistory(user.getUserId());
		//ArrayList<UserPasswordHistory> passwordHistoryList = userService.getPasswordHistory(user.getUserId(),1);
		String passwordExpirecount = propertyService.getPropertyValue(BluefinWebPortalConstants.PASSWORDEXPIREAFTER);
		String passwordWarncount = propertyService.getPropertyValue(BluefinWebPortalConstants.PASSWORDEXPIREWARNBEFORE);
		int passwordExpireAfter = org.apache.commons.lang3.StringUtils.isNotEmpty(passwordExpirecount) ? Integer.parseInt(passwordExpirecount) : BluefinWebPortalConstants.PASSWORDEXPIREAFTERCOUNT ;
		int passwordWarnWithIn = org.apache.commons.lang3.StringUtils.isNotEmpty(passwordWarncount) ? Integer.parseInt(passwordWarncount) : BluefinWebPortalConstants.PASSWORDEXPIREWARNBEFORECOUNT;
		DateTime dateModified;
		DateTime currentDateTime = new DateTime(DateTimeZone.UTC);
		if(passwordHistoryList.isEmpty()) {
			dateModified = user.getDateCreated();
		}else {
			dateModified = passwordHistoryList.get(0).getDateModified();
			if(dateModified == null) {
				dateModified = user.getDateCreated();
			}
		}
		if(dateModified == null) {
			dateModified = new DateTime(DateTimeZone.UTC);
		}
		int daysDiff = Days.daysBetween(dateModified, currentDateTime).getDays();
		if(passwordExpireAfter>=0 && passwordWarnWithIn>=0 && (passwordExpireAfter-passwordWarnWithIn)>=0) {
			if(daysDiff>=(passwordExpireAfter-passwordWarnWithIn) && daysDiff<=passwordExpireAfter) {
				if(response!=null) {
					response.setWarn("Please change your password, Your password will be expire "+((passwordExpireAfter-daysDiff) == 0 ? "today" : "in next "+(passwordExpireAfter-daysDiff) +" days"));
					response.setChangePasswordWithIn(daysDiff);
				}
			}
			else if(daysDiff>passwordExpireAfter) {
				if(response!=null) {
					response.setWarn("Your password has been expired");
					response.setChangePasswordWithIn(0);
				}
				isPasswordExpire = true;
			}
		}
		
		return isPasswordExpire;
	}
}
