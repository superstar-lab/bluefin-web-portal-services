package com.mcmcg.ico.bluefin.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.mcmcg.ico.bluefin.service.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
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
import com.mcmcg.ico.bluefin.repository.UserPreferenceDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.ThirdPartyAppResource;
import com.mcmcg.ico.bluefin.rest.resource.TokenResponse;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.model.SecurityUser;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
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
	@Autowired
	private UserLoginHistoryRepoService userLoginHistoryRepoService;
	@Autowired
	private UserLookUpService userLookUpService;

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void authenticate(final String username, final String password) {
		LOGGER.info("Entering to authenticate");
		User user = userDAO.findByUsername(username);
		UserLoginHistory userLoginHistory = new UserLoginHistory();
		userLoginHistory.setUsername(username);
		userLoginHistory.setPassword(password);
		
		DateTime currentTimeUTC = new DateTime(DateTimeZone.UTC);
		LOGGER.debug("user is ={} ",user);

		userLoginHistoryRepoService.saveUserLoginHistoryAuthentication(user, userLoginHistory, username);
		
		checkUserStatusForLock(user, userLoginHistory, currentTimeUTC);
		
		if(isPasswordExpire(user, null)) {
			userLoginHistoryRepoService.saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_PASSWORD_EXPIRED.getValue());
		}
		else {
			user.setWrongPasswordCounter(0);
			user.setLastLogin(currentTimeUTC);
			userLookUpService.updateUserLookUp(user);
			userLoginHistoryRepoService.saveUserLoginHistory(userLoginHistory, MessageCode.SUCCESS.getValue());
		}
		
		LOGGER.info("Exit from authenticate");
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
	
	/**
	 * Generates a password of the specified size with upper case letters, lower case letters, numbers and special symbols
	 * @param size, of the password required to be return by the function
	 * @return a string of the specified size with random characters
	 */
	private static String generateString(int size) {
		String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!^#$%*";
        Random rand = new Random();
        int[] order = new int[3];
        
        order[0] = rand.nextInt(3);
        order[1] = order[0];
        while(order[1]==order[0]) order[1] = rand.nextInt(3);
        order[2] = order[0];
        while(order[2]==order[0] || order[2]==order[1]) order[2] = rand.nextInt(3);
        StringBuilder result = new StringBuilder();
        while(result.length()<size-1) {
            switch(order[result.length() % 3])
            {
                case 0:
                    result.append(upperCaseLetters.charAt(rand.nextInt(upperCaseLetters.length())));
                    break;
                case 1:
                    result.append(lowerCaseLetters.charAt(rand.nextInt(lowerCaseLetters.length())));
                    break;
                default:
                    result.append(numbers.charAt(rand.nextInt(numbers.length())));
                    break;
            }
        }
        result.append(symbols.charAt(rand.nextInt(symbols.length())));
        return result.toString();
    }

	public void resetPassword(final String username) {
		LOGGER.info("Entering to reset Password");
		User user = userService.getUser(username);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("No user found with username '%s', reseting password.", username));
		} 
		List<UserPasswordHistory> userPasswordHistory = userService.getPasswordHistory(user.getUserId()); 
		if(userPasswordHistory!=null && !userPasswordHistory.isEmpty()) {
			DateTime modifiedDate = userPasswordHistory.get(0).getDateModified();
			DateTime today = new DateTime(DateTimeZone.UTC);
			DateTime todayMinus = today.minusHours(3);
			
			int hours = Hours.hoursBetween(modifiedDate, today).getHours();
			int hours2 = Hours.hoursBetween(todayMinus, today).getHours();
			if(hours <= 24) {
				throw new CustomBadRequestException("Password being resent in less than 24 hours, modified date:" +
						modifiedDate.toString() +
						" today: " + today.toString() + 
						" hours calculated " + hours +
						" subtracted date " + todayMinus.toString() +
						" hours with subtracted hours " + hours2);
			}
		}
		LOGGER.debug("user is:- ={} ",user);
		LOGGER.debug("Reseting password of user: {}", username);
		final String newPassword = generateString(15);
		final String link = "/api/users/" + username + "/password";
		final String token = generateNewToken(username, TokenType.FORGOT_PASSWORD, link);
		String name = user.getFirstName();
		String lastName = user.getLastName();
		// name will contain user full name. If this is found null on database will be used the username
		name = name == null ? "" : name.trim();
		lastName = lastName == null ? "" : lastName.trim();
		name = name.length() > 0 && lastName.length() > 0 ? name + " " + lastName : name + lastName;
		name = name.length() > 0 ? name : username;
		// required following object to be able to update the password
		UpdatePasswordResource userInfo = new UpdatePasswordResource();
		userInfo.setOldPassword(user.getPassword());
		userInfo.setNewPassword(newPassword);
		// updating password
		userService.updateUserPassword(username, userInfo, token);
		// preparing for sending email
		String subject = propertyService.getPropertyValue("RESET_PASSWORD_EMAIL_SUBJECT");
		String content = propertyService.getPropertyValue("RESET_PASSWORD_EMAIL_BODY");
		// preparing the content or body of the email 
		if(content!=null) {
			content = content.
					replace("~u", username).
					replace("~U", name).
					replace("~t", token).
					replace("~n", "\n").
					replace("~N", "\n\n");
		}
		// Send email
		LOGGER.info("ready to send email");
		
		emailService.sendEmail(user.getEmail(),
				subject!=null ? subject : "Bluefin web portal: password reset request",
				content!=null ? content : "Password reset complete for user " + username + ", create new password using link bellow:\n\n" + "https://bluefin.mcmcg.com/login/reset?user=" + username + "&token=" + token + "\n\nBluefin web portal automated email");
				
		LOGGER.info("email sent.");
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
		try {
			LOGGER.debug("authentication size is ={}", authentication.getAuthorities().size());
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				hasPermission = BluefinWebPortalConstants.PRODUCT_CONFIGURATION.equals(authority.getAuthority());
				LOGGER.debug("hasPermission ={} ",hasPermission);
				if (Boolean.TRUE.equals(hasPermission)) {
					break;
				}
			}
		}catch(Exception ex) {
			LOGGER.error("sessionHasPermissionToManageAllLegalEntities authentication cannot be NULL {}",ex.getMessage());
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
		
		List<UserPasswordHistory> passwordHistoryList = userService.getPasswordHistory(user.getUserId());
		String passwordExpirecount = propertyService.getPropertyValue(BluefinWebPortalConstants.PWEXPIREAFTER);
		String passwordWarncount = propertyService.getPropertyValue(BluefinWebPortalConstants.PWEXPIREWARNBEFORE);
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
		
		return checkPasswordExpire(passwordExpireAfter, passwordWarnWithIn, daysDiff, response);
		
	}
	
	private void checkUserStatusForLock(User user, UserLoginHistory userLoginHistory, DateTime currentTimeUTC) {
		int accountLockedDurationSecsVal;
		String accountLockedDurationSecs;
		
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
				String message = LoggingUtil.invalidLoginAttempts("User : ", user.getUsername(), BluefinWebPortalConstants.SEPARATOR,
						"Reason : User is ", UserStatus.LOCKED.getStatus());
				LOGGER.error(message);

				userLoginHistoryRepoService.saveUserLoginHistory(userLoginHistory, MessageCode.ERROR_USER_IS_LOCKED.getValue());
				throw new AccessDeniedException("Account is Locked.");
			}
		}
	}

	public boolean checkPasswordExpire(int passwordExpireAfter, int passwordWarnWithIn, int daysDiff, AuthenticationResponse response) {
		boolean isPasswordExpire = false;
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
	
	public boolean hasPermissionToManageAllUser(Authentication authentication) {
		LOGGER.info("Entering to Has Permission To Manage All User");
		Boolean hasPermission = false;
		try {
			LOGGER.debug("authentication size to manage all user is ={}", authentication.getAuthorities().size());
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				hasPermission = "MANAGE_ALL_USERS".equals(authority.getAuthority());
				LOGGER.debug("hasPermission value ={} ",hasPermission);
				if (Boolean.TRUE.equals(hasPermission)) {
					break;
				}
			}
		}catch(Exception ex) {
			LOGGER.error("hasPermissionToManageAllUser authentication cannot be NULL {}",ex.getMessage());
		}
		LOGGER.info("Exit from Has Permission To Manage All User");
		return hasPermission;
	}
}
