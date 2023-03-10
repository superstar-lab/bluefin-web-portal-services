package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.*;
import com.mcmcg.ico.bluefin.repository.*;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.*;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private UserDAO userDAO;
	@Autowired
	private RoleService roleService;
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private EmailService emailService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private UserRoleDAO userRoleDAO;
	@Autowired
	private UserLegalEntityAppDAO userLegalEntityAppDAO;
	@Autowired
	private UserPreferenceDAO userPreferenceDAO;
	@Autowired
	private PropertyDAO propertyDAO;

	private static final String REGISTER_USER_EMAIL_SUBJECT = "Bluefin web portal: Register user email";
	private static final String DEACTIVATE_ACCOUNT_EMAIL_SUBJECT = "Bluefin web portal: Deactivated account";
	private static final Object[] FILE_HEADER = { "#", "User Name", "First Name", "Last Name", "Role Name", "Email",
			"Date Created", "Date Modified", "Last Login", "Last Date Password Modified","Status"};

	/**
	 * Get user information by username
	 *
	 * @param username
	 * @return UserResource object
	 * @throws CustomBadRequestException
	 *             user not found
	 */
	public UserResource getUserInfomation(String username) {
		return new UserResource(getUser(username));
	}

	/**
	 * Get user object by username
	 *
	 * @param username
	 * @return user object
	 * @throws CustomNotFoundException
	 *             when username is not found
	 */
	public User getUser(final String username) {
		User user = userDAO.findByUsername(username);
		if (user == null) {
			throw new CustomNotFoundException("Unable to find user by username provided: " + username);
		}
		List<UserRole> userRoles = userRoleDAO.findByUserId(user.getUserId());
		LOGGER.debug("userRoles size ={} ",userRoles != null ? userRoles.size() : 0);
		user.setRoles(userRoles);
		List<UserLegalEntityApp> userLegalEntityApps = userLegalEntityAppDAO.findByUserId(user.getUserId());
		LOGGER.debug("userLegalEntityApps size :={} ",userLegalEntityApps != null ? userLegalEntityApps.size() : 0);
		user.setLegalEntities(userLegalEntityApps);
		user.setSelectedTimeZone(userPreferenceDAO.getSelectedTimeZone(user.getUserId()));
		return user;
	}

	public Iterable<User> getUsers(List<String> search, Integer page, Integer size, String sort) {
		LOGGER.info("Entering to get User list ");
		Map<String,String> filterMap = new HashMap<>(7);
		if(search != null && !search.isEmpty()) {
			for(String searchParam:search){
				checkUser(searchParam, filterMap);
			}
		}
		Page<User> result = userDAO.findAllWithDynamicFilter(search, QueryDSLUtil.getPageRequest(page, size, sort),filterMap);
		if (page > result.getTotalPages() && page != 0) {
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		LOGGER.info("exiting from get User list ");
		return result;
	}

	/**
	 * Gets the legal entities by user name
	 *
	 * @param username
	 * @return list of legal entities owned by the user with the user name given
	 *         by parameter, empty list if user not found
	 */
	public List<LegalEntityApp> getLegalEntitiesByUser(final String username) {
		User user = userDAO.findByUsername(username);
		LOGGER.debug("user id :={} ", user == null ? null : user.getUserId());
		List<LegalEntityApp> list = new ArrayList<>();
		if (user != null) {
			for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
				long legalEntityAppId = userLegalEntityApp.getUserLegalEntityAppId();
				list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));
			}
		}
		return (user == null || userLegalEntityAppDAO.findByUserId(user.getUserId()).isEmpty())
				? new ArrayList<>() : list;
	}

	public UserResource registerNewUserAccount(RegisterUserResource userResource) {
		final String username = userResource.getUsername();
		if (existUsername(username)) {
			String message = LoggingUtil.adminAuditInfo("User Creation Request", BluefinWebPortalConstants.SEPARATOR,
					"Unable to create the account, this username already exists : ", username);
			LOGGER.error(message);

			throw new CustomBadRequestException(
					"Unable to create the account, this username already exists: " + username);
		}

		User newUser = userResource.toUser(roleService.getRolesByIds(userResource.getRoles()),
				getLegalEntityAppsByIds(userResource.getLegalEntityApps()));
		newUser.setStatus("NEW");
		newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

		long userId = userDAO.saveUser(newUser);
		LOGGER.debug("userid ={} ", userId);
		UserResource newUserResource = new UserResource(getUser(username));

		try{
			//Create/Update User preference of time zone
			newUser.setUserId(userId);
			updaUserPrefernce(newUser);
		}catch(Exception ex){
			LOGGER.error("Error while update user prefrence time zone", ex);
		}

		// Send email
		final String link = BluefinWebPortalConstants.APIUSER + username + BluefinWebPortalConstants.PASSLINK;
		final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
		String content = BluefinWebPortalConstants.WELCOMECONTENT
				+ BluefinWebPortalConstants.USERNAME + username + BluefinWebPortalConstants.CREATEPASSCONTENT
				+ propertyService.getPropertyValue(BluefinWebPortalConstants.REGISTERUSEREMAIL) + BluefinWebPortalConstants.USERVALUE + username + BluefinWebPortalConstants.TOKENVALUE
				+ token;
		emailService.sendEmail(newUser.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);

		return newUserResource;
	}

	/**
	 * Get all legal entity app objects by the entered ids
	 *
	 * @param legalEntityAppsIds
	 *            list of legal entity apps ids that we need to find
	 * @return list of legal entity apps
	 * @throws CustomBadRequestException
	 *             when at least one id does not exist
	 */
	public List<LegalEntityApp> getLegalEntityAppsByIds(Set<Long> legalEntityAppsIds) {
		List<LegalEntityApp> result = legalEntityAppDAO.findAll(new ArrayList<Long>(legalEntityAppsIds));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("LegalEntityApp result size : {}",result.size());
		}
		if (result != null && result.size() == legalEntityAppsIds.size()) {
			return result;
		}

		// Create a detail error
		if (result == null || result.isEmpty()) {
			throw new CustomBadRequestException(
					"The following legal entity apps don't exist.  List = " + legalEntityAppsIds);
		}

		Set<Long> legalEntityAppsNotFound = legalEntityAppsIds.stream().filter(
				x -> !result.stream().map(LegalEntityApp::getLegalEntityAppId).collect(Collectors.toSet()).contains(x))
				.collect(Collectors.toSet());

		throw new CustomBadRequestException(
				"The following legal entity apps don't exist.  List = " + legalEntityAppsNotFound);
	}

	public boolean existUsername(final String username) {
		LOGGER.info("existUsername");
		return userDAO.findByUsername(username) == null ? false : true;
	}

	/**
	 * Update the profile information of an already stored user
	 *
	 * @param username
	 * @param userResource
	 * @param loginUserName
	 * @return userResource with all the user information
	 * @throws CustomNotFoundException
	 */
	public UserResource updateUserProfile(String username, UpdateUserResource userResource, String loginUserName) {
		User user = getUser(username);

		LOGGER.debug(" user={} ",user);
		// Updating fields from existing user
		user.setFirstName(userResource.getFirstName());
		user.setLastName(userResource.getLastName());
		user.setEmail(userResource.getEmail());
		user.setDateUpdated(new DateTime());
		String modifiedBy = StringUtils.isNotBlank(loginUserName) ? loginUserName : null;
		user.setSelectedTimeZone(userResource.getSelectedTimeZone());
		//backup user's role and legalEntities, will set these roles and legalEntities in api response.
		Collection<UserRole> userRoleList= user.getRoles();
		Collection<UserLegalEntityApp> userLegalEntities = user.getLegalEntities();
		//We are setting empty collectionn object not  to update roles in case of password update
		user.setRoles(Collections.emptyList());
		user.setLegalEntities(Collections.emptyList());
		//	if (StringUtils.equals(username, loginUserName)) {
		updaUserPrefernce(user);
		//	}
		userDAO.updateUser(user, modifiedBy);

		//set user role and legalEntities in API response.
		user.setRoles(userRoleList);
		user.setLegalEntities(userLegalEntities);
		return new UserResource(user);
	}

	private void updaUserPrefernce(User user) {
		long preferenceId = userPreferenceDAO.findPreferenceIdByPreferenceKey(UserPreferenceEnum.USERTIMEZONEPREFRENCE.toString());
		LOGGER.debug("preferenceId ={} ",preferenceId);
		/**
		 * fetching UserPreferenceID from UserPreference_Lookup table based on userId & PreferenceID.
		 */
		UserPreference userPreference = userPreferenceDAO.findUserPreferenceIdByPreferenceId(user.getUserId(), preferenceId);
		LOGGER.debug("userPreference ={} ",userPreference);

		if (userPreference != null && userPreference.getUserPrefeenceID() != null) {
			userPreference.setPreferenceValue(user.getSelectedTimeZone());
			userPreferenceDAO.updateUserTimeZonePreference(userPreference);
		}else{
			UserPreference userPreferenceToInsert = createUserPreference(preferenceId, user.getUserId(), user.getSelectedTimeZone());
			userPreferenceDAO.insertUserTimeZonePreference(userPreferenceToInsert);
		}
	}

	private UserPreference createUserPreference(long preferenceId, Long userId, String selectedTimeZone) {
		LOGGER.info("create User Preference ");
		UserPreference userPreference = new UserPreference();
		userPreference.setPreferenceKeyID(preferenceId);
		userPreference.setPreferenceValue(selectedTimeZone);
		userPreference.setUserID(userId);
		return userPreference;
	}

	/**
	 * Update the roles of an already stored user
	 *
	 * @param username
	 * @param rolesIds
	 * @param loginUser
	 * @return userResource with all the user information
	 * @throws CustomNotFoundException
	 */
	public User updateUserRoles(final String username, final Set<Long> rolesIds, String loginUser) {


		User userToUpdate = getUser(username);

		LOGGER.debug("userToUpdate ={}",userToUpdate);
		// User wants to clear roles from user
		if (rolesIds.isEmpty()) {
			String message = LoggingUtil.adminAuditInfo("User Profile Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", username, " must have at least one role assign to him.");
			LOGGER.error(message);

			throw new CustomBadRequestException("User MUST have at least one role assign to him.");
		}

		// Validate and load existing roles
		Map<Long, Role> newMapOfRoles = roleService.getRolesByIds(rolesIds).stream()
				.collect(Collectors.toMap(Role::getRoleId, r -> r));

		LOGGER.debug("newMapOfRoles size ={} ",newMapOfRoles.size());
		// Temporal list of roles that we need to keep in the user role list
		Set<Long> rolesToKeep = new HashSet<>();
		Set<Long> rolesToRemove = new HashSet<>();
		// Update current role list from user
		Iterator<UserRole> iter = userToUpdate.getRoles().iterator();
		while (iter.hasNext()) {
			UserRole element = iter.next();

			Role role = newMapOfRoles.get(element.getRoleId());
			if (role == null) {
				iter.remove();//
				rolesToRemove.add(element.getUserRoleId());
			} else {
				iter.remove();// No need to have this Roles objetct , as it is already in db associated with this user
				rolesToKeep.add(element.getRoleId());
			}
		}

		// Correct this when fixing code for User.
		// Add new roles to the user but ignoring the existing ones
		for (Entry<Long,Role> roleEntry : newMapOfRoles.entrySet()) {
			if (!rolesToKeep.contains(roleEntry.getKey())) {
				userToUpdate.addRole(roleEntry.getValue());
			}
		}

		userToUpdate.setDateUpdated(new DateTime());
		String modifiedBy = StringUtils.isNotBlank(loginUser) ? loginUser : null;
		removeRolesFromUser(rolesToRemove);
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setLegalEntities(Collections.emptyList());
		LOGGER.info("ready to update user ");
		userDAO.updateUser(userToUpdate, modifiedBy);
		return getUser(username);

	}
	public User updateUserRoles(final String username, final Set<Long> rolesIds) {
		return updateUserRoles(username, rolesIds, null);
	}

	private void removeRolesFromUser(Set<Long> rolesToRemove) {
		LOGGER.info("remove Roles FromUser ");
		userRoleDAO.deleteUserRoleById(rolesToRemove);
	}

	/**
	 * Update the legalEntities of an already stored user
	 *
	 * @param username
	 * @param legalEntityAppsIds
	 * @param loginUser
	 * @return user with all the user information
	 * @throws CustomNotFoundException
	 */
	public User updateUserLegalEntities(final String username, final Set<Long> legalEntityAppsIds, String loginUser) {

		User userToUpdate = getUser(username);

		LOGGER.debug("userToUpdate ={} ",userToUpdate);
		// User wants to clear legal entity apps from user
		if (legalEntityAppsIds == null || legalEntityAppsIds.isEmpty()) {
			throw new CustomBadRequestException("User MUST have at least one legal entity assign to him.");
		}

		// Validate and load existing legal entity apps
		Map<Long, LegalEntityApp> newMapOfLegalEntityApps = getLegalEntityAppsByIds(legalEntityAppsIds).stream()
				.collect(Collectors.toMap(LegalEntityApp::getLegalEntityAppId, l -> l));

		LOGGER.debug("newMapOfLegalEntityApps size ={} ",newMapOfLegalEntityApps.size());
		// Temporal list of legal entity apps that we need to keep in the user
		// legal entity app list
		Set<Long> legalEntityAppsToKeep = new HashSet<>();
		Set<Long> legalEntityAppsToRemove = new HashSet<>();
		// Update current role list from user
		Iterator<UserLegalEntityApp> iter = userToUpdate.getLegalEntities().iterator();
		while (iter.hasNext()) {
			UserLegalEntityApp element = iter.next();

			LegalEntityApp legalEntityApp = newMapOfLegalEntityApps.get(element.getLegalEntityAppId());
			if (legalEntityApp == null) {
				iter.remove();
				legalEntityAppsToRemove.add(element.getUserLegalEntityAppId());
			} else {
				iter.remove();
				legalEntityAppsToKeep.add(element.getLegalEntityAppId());
			}
		}

		// Add new roles to the user but ignoring the existing ones
		for (Entry<Long,LegalEntityApp> legalEntityApp : newMapOfLegalEntityApps.entrySet()) {
			if (!legalEntityAppsToKeep.contains(legalEntityApp.getKey())) {
				userToUpdate.addLegalEntityApp(legalEntityApp.getValue());
			}
		}
		userToUpdate.setDateUpdated(new DateTime());
		String modifiedBy = StringUtils.isNotBlank(loginUser) ? loginUser : null;
		removeLegalEntityFromUser(legalEntityAppsToRemove);
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setRoles(Collections.emptyList());
		LOGGER.debug("ready to update user");
		userDAO.updateUser(userToUpdate, modifiedBy);
		return getUser(username);

	}

	public User updateUserLegalEntities(final String username, final Set<Long> legalEntityAppsIds) {
		return updateUserLegalEntities(username, legalEntityAppsIds, null);
	}

	public void removeLegalEntityFromUser(Collection<Long> legalEntityAppsToRemove) {
		LOGGER.info("remove LegalEntity From User");
		userLegalEntityAppDAO.deleteUserLegalEntityAppById(legalEntityAppsToRemove);
	}

	/**
	 * Validates if the current legal entities of the user that tries to get the
	 * information are valid by checking the values of the request with the ones
	 * owned by the user
	 *
	 * @param authentication
	 * @param legalEntitiesToVerify
	 */
	public boolean hasUserPrivilegesOverLegalEntities(Authentication authentication, Set<Long> legalEntitiesToVerify) {
		if (hasPermissionToManageAllUsers(authentication)) {
			return true;
		}
		// Get Legal Entities from user name
		Set<Long> userLegalEntities = getLegalEntitiesByUser(authentication.getName()).stream()
				.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

		LOGGER.debug("userLegalEntities size ={} ",userLegalEntities.size());
		return legalEntitiesToVerify.stream()
				.filter(verifyLegalEntityId -> !userLegalEntities.contains(verifyLegalEntityId))
				.collect(Collectors.toSet()).isEmpty();
	}

	/**
	 * Verify if user has authority to manage all user.
	 *
	 * @param authentication
	 *
	 * @return status
	 */
	public boolean hasPermissionToManageAllUsers(Authentication authentication) {
		Boolean hasPermission = false;
		try {
			LOGGER.debug("authentication ={} ",authentication.getAuthorities().size());
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				String userAuthority = authority.getAuthority();
				if ("ADMINISTRATIVE".equals(userAuthority) || "MANAGE_ALL_USERS".equals(userAuthority)) {
					hasPermission = true;
				}
				if (Boolean.TRUE.equals(hasPermission)) {
					break;
				}
			}
			LOGGER.debug("hasPermission ={} ",hasPermission);
		}catch(Exception ex) {
			LOGGER.error("hasPermissionToManageAllUsers authentication cannot be NULL {}",ex.getMessage());
		}
		return hasPermission;
	}

	/**
	 * This method will return true if both users have a common legal entity,
	 * false in other case
	 *
	 * @param authentication
	 * @param usernameToUpdate
	 * @return true if the request user has related legal entities with the user
	 *         he wants to CRUD
	 * @throws CustomNotFoundException
	 *             username not found
	 */
	public boolean belongsToSameLegalEntity(Authentication authentication, final String usernameToUpdate) {
		final String username = authentication.getName();
		LOGGER.debug(" username ={} ",username);
		if (usernameToUpdate.equals(username)) {
			return true;
		}

		// Verify if user that needs to be updated exist
		User userToUpdate = getUser(usernameToUpdate);
		LOGGER.debug("userToUpdate ={} ",userToUpdate.getUserId());
		if (hasPermissionToManageAllUsers(authentication)) {
			return true;
		}
		// Get Legal Entities from consultant user
		Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
				.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());
		LOGGER.debug("userLegalEntities size ={} ",userLegalEntities.size());
		// Get Legal Entities from user that will be updated
		List<LegalEntityApp> list = new ArrayList<>();
		for (UserLegalEntityApp userLegalEntityApp : userToUpdate.getLegalEntities()) {
			long legalEntityAppId = userLegalEntityApp.getUserLegalEntityAppId();
			list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));
		}
		Set<Long> legalEntitiesToVerify = list.stream()
				.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

		return !legalEntitiesToVerify.stream().filter(userLegalEntities::contains).collect(Collectors.toSet())
				.isEmpty();
	}

	/**
	 * Update the password of an already stored user
	 *
	 * @param username
	 * @param updatePasswordResource
	 * @return user with all the user information
	 * @throws CustomNotFoundException
	 * @throws CustomBadRequestException
	 */
	public User updateUserPassword(String username, final UpdatePasswordResource updatePasswordResource,
								   final String token) {
		String usernameVal = "me".equals(username) ? tokenUtils.getUsernameFromToken(token) : username;
		LOGGER.debug("Username : {}",usernameVal);
		String tokenType = tokenUtils.getTypeFromToken(token);
		LOGGER.debug("TokenType : {}",tokenType);

		User userToUpdate = getUser(usernameVal);
		LOGGER.debug("UserToUpdate : {} ",userToUpdate.getUserId());

		List<Object> passwordHistoryAndLastCount = validateUserName(tokenType, userToUpdate, updatePasswordResource, usernameVal);

		@SuppressWarnings("unchecked")
		List<UserPasswordHistory> passwordHistoryList = (List<UserPasswordHistory>) passwordHistoryAndLastCount.get(0);
		int lastPasswordCount = (int) passwordHistoryAndLastCount.get(1);

		String userPreviousPasword = userToUpdate.getPassword();
		setStatus(tokenType,userToUpdate);
		userToUpdate.setPassword(passwordEncoder.encode(updatePasswordResource.getNewPassword()));
		userToUpdate.setDateUpdated(new DateTime());
		String modifiedBy = StringUtils.isNotBlank(username) ? username : null;
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setRoles(Collections.emptyList());
		userToUpdate.setLegalEntities(Collections.emptyList());
		LOGGER.info("Ready to update user");
		userDAO.updateUser(userToUpdate, modifiedBy);
		if(passwordHistoryList.size()<lastPasswordCount-1) {
			userDAO.savePasswordHistory(userToUpdate, username, userPreviousPasword);
		}
		else {
			if(!passwordHistoryList.isEmpty()) {
				userDAO.updatePasswordHistory(passwordHistoryList.get(passwordHistoryList.size()-1).getPasswordHistoryID(),username,userPreviousPasword);
			}

		}
		LOGGER.info("Ready to find user by id");
		return userDAO.findByUserId(userToUpdate.getUserId());
	}

	private void setStatus(String tokenType,User userToUpdate){
		if (tokenType.equals(TokenType.REGISTER_USER.name())) {
			userToUpdate.setStatus("ACTIVE");
		}
		if (tokenType.equals(TokenType.FORGOT_PASSWORD.name()) && userToUpdate.getStatus().contentEquals("NEW")){
			userToUpdate.setStatus("ACTIVE");
		}
	}

	public void userActivation(ActivationResource activationResource, String modifiedBy) {
		Boolean activate = activationResource.isActivate();
		LOGGER.debug("activate ={}",activate);
		List<String> notFoundUsernames = new ArrayList<>();

		LOGGER.debug("activationResource size ={} ", activationResource.getUsernames() == null ? null : activationResource.getUsernames().size());
		for (String username : activationResource.getUsernames()) {

			if (username == null) {
				throw new CustomBadRequestException("An authorization token is required to request this resource");
			}

			String status = activate ? "NEW" : "INACTIVE";
			User user = userDAO.findByUsername(username);
			if (user == null) {
				notFoundUsernames.add(username);
			} else {
				activateAccount(user, status, modifiedBy);
			}
		}
		if (!notFoundUsernames.isEmpty()) {
			throw new CustomNotFoundException("Unable to find users by usernames provided: " + notFoundUsernames);
		}
	}

	private void activateAccount(User userToUpdate, String status, String modifiedBy) {
		String username = userToUpdate.getUsername();
		LOGGER.debug("username:= {} ",username);
		if (!userToUpdate.getStatus().equalsIgnoreCase( status)) {
			userToUpdate.setStatus(status);
			if ("NEW".equals(status)) {

				// Send email
				final String link = BluefinWebPortalConstants.APIUSER + username + BluefinWebPortalConstants.PASSLINK;
				final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
				String content = BluefinWebPortalConstants.WELCOMECONTENT
						+ BluefinWebPortalConstants.USERNAME + username + BluefinWebPortalConstants.CREATEPASSCONTENT
						+ propertyService.getPropertyValue(BluefinWebPortalConstants.REGISTERUSEREMAIL) + BluefinWebPortalConstants.USERVALUE + username + BluefinWebPortalConstants.TOKENVALUE
						+ token;
				emailService.sendEmail(userToUpdate.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);
			} else {
				String content = "Your account has been deactivated. \n\n"
						+ "Please feel free to contact your system administratior. \n\n";
				emailService.sendEmail(userToUpdate.getEmail(), DEACTIVATE_ACCOUNT_EMAIL_SUBJECT, content);
			}
			// Why we need to update roles and LE while activating/deactivating user, so make Roles/LE list as empty.[Matloob]
			userToUpdate.setRoles(Collections.emptyList());
			userToUpdate.setLegalEntities(Collections.emptyList());

			LOGGER.info("ready to update user ");
			long userId = userDAO.updateUserStatus(userToUpdate, modifiedBy);
			LOGGER.debug("userId ",userId);
			//TOOD.................Why are you calling below operation again, I have commented this [Matloob]
		}
		else {
			// Send email
			if("INACTIVE".equalsIgnoreCase(status)) {
				String content = "Your account has been deactivated. \n\n"
						+ "Please feel free to contact your system administratior. \n\n";
				emailService.sendEmail(userToUpdate.getEmail(), DEACTIVATE_ACCOUNT_EMAIL_SUBJECT, content);
			}
			else {
				final String link = BluefinWebPortalConstants.APIUSER + username + BluefinWebPortalConstants.PASSLINK;
				final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
				String content = BluefinWebPortalConstants.WELCOMECONTENT
						+ BluefinWebPortalConstants.USERNAME + username + BluefinWebPortalConstants.CREATEPASSCONTENT
						+ propertyService.getPropertyValue(BluefinWebPortalConstants.REGISTERUSEREMAIL) + BluefinWebPortalConstants.USERVALUE + username + BluefinWebPortalConstants.TOKENVALUE
						+ token;
				emailService.sendEmail(userToUpdate.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);
			}
		}
	}

	private boolean isValidOldPassword(final String oldPassword, final String currentUserPassword) {
		LOGGER.info("is Valid Old Password");
		if (StringUtils.isEmpty(oldPassword)) {
			throw new CustomBadRequestException("oldPassword must not be empty");
		}

		LOGGER.info("Exiting from is Valid Old Password");
		return passwordEncoder.matches(oldPassword, currentUserPassword);
	}

	public User findByUsername(String userName){
		return userDAO.findByUsername(userName);
	}

	public List<UserPasswordHistory> getPasswordHistory(final Long userId) {
		return userDAO.getPasswordHistoryById(userId);
	}

	/**public ArrayList<UserPasswordHistory> getPasswordHistory(final Long userId, int limit) {
	 ArrayList<UserPasswordHistory> userList = userDAO.getPasswordHistoryById(userId, limit);
	 if (userList.size()<0) {
	 throw new CustomNotFoundException("Unable to find user by userID provided: " + userList.size());
	 }
	 return userList;
	 }*/

	public void checkUser(String searchParam, Map<String,String> filterMap) {
		String[] str1 = searchParam.split(":");
		if ("legalEntities".equalsIgnoreCase(str1[0]) || "roles".equalsIgnoreCase(str1[0]) ) {
			str1[1] = str1[1].replace("[", "");
			str1[1] = str1[1].replace("]", "");
			filterMap.put(str1[0], str1[1]);
		} else {
			if("status".equalsIgnoreCase(str1[0])){
				filterMap.put(str1[0], str1[1]);
			}
			else{
				filterMap.put(str1[0], "%".concat(str1[1]).concat("%"));
			}
		}
	}

	public List<Object> validateUserName(String tokenType, User userToUpdate, final UpdatePasswordResource updatePasswordResource, String usernameVal) {
		if (usernameVal == null || tokenType == null) {
			String message = LoggingUtil.adminAuditInfo("User Password Updation Request:", BluefinWebPortalConstants.SEPARATOR,
					"Password updation failed for User: ", usernameVal, BluefinWebPortalConstants.SEPARATOR,
					"An authorization token is required to request this resource..");
			LOGGER.error(message);

			throw new CustomBadRequestException("An authorization token is required to request this resource");
		}

		return validateUserNameAuthentication(tokenType,userToUpdate,updatePasswordResource,usernameVal);
	}

	public List<Object> validateUserNameAuthentication(String tokenType, User userToUpdate, final UpdatePasswordResource updatePasswordResource, String usernameVal) {
		if ((tokenType.equals(TokenType.AUTHENTICATION.name()) || tokenType.equals(TokenType.APPLICATION.name()))
				&& !isValidOldPassword(updatePasswordResource.getOldPassword(), userToUpdate.getPassword())) {
			String message = LoggingUtil.adminAuditInfo("User Password Updation Request::", BluefinWebPortalConstants.SEPARATOR,
					"Password updation failed for User:: ", usernameVal, BluefinWebPortalConstants.SEPARATOR,
					"The old password is incorrect...");
			LOGGER.error(message);

			throw new CustomBadRequestException("The old password is incorrect.");
		}

		return checkInPasswordHistory(userToUpdate,updatePasswordResource,usernameVal);
	}

	public List<Object> checkInPasswordHistory(User userToUpdate, final UpdatePasswordResource updatePasswordResource, String usernameVal) {
		boolean isPasswordDeleted = false;
		List<UserPasswordHistory> passwordHistoryList = getPasswordHistory(userToUpdate.getUserId());
		//delete old password from password history if password match count changed
		String lastPwCount = propertyService.getPropertyValue(BluefinWebPortalConstants.MATCHLASTPWCOUNT);
		int lastPasswordCount = org.apache.commons.lang3.StringUtils.isNotEmpty(lastPwCount) ? Integer.parseInt(lastPwCount) : BluefinWebPortalConstants.MATCHLASTPASSWORD;
		int passwordHistoryCount = passwordHistoryList.size();
		for (UserPasswordHistory userPasswordHistory : passwordHistoryList) {
			LOGGER.debug("userPasswordHistory : {} ",userPasswordHistory);
			if(passwordHistoryCount>lastPasswordCount-1) {
				if(passwordHistoryCount>0){
					userDAO.deletePasswordHistory(passwordHistoryList.get(passwordHistoryCount-1).getPasswordHistoryID(),userToUpdate.getUserId());
					isPasswordDeleted = true;
					passwordHistoryCount--;
				}
			}
			else {
				break;
			}
		}

		return checkPasswordMatch(isPasswordDeleted, passwordHistoryList, userToUpdate, updatePasswordResource, usernameVal, lastPasswordCount);
	}

	public List<Object> checkPasswordMatch(boolean isPasswordDeleted, List<UserPasswordHistory> passwordHistoryList, User userToUpdate,
										   final UpdatePasswordResource updatePasswordResource, String usernameVal, int lastPasswordCount) {
		boolean isPasswordMatch = false;
		if(isPasswordDeleted) {
			passwordHistoryList = getPasswordHistory(userToUpdate.getUserId());
		}
		for(UserPasswordHistory userPasswordHistory : passwordHistoryList) {
			if(passwordEncoder.matches(updatePasswordResource.getNewPassword(), userPasswordHistory.getPreviousPassword())) {
				isPasswordMatch = true;
				break;
			}
		}

		if (lastPasswordCount>0 && (passwordEncoder.matches(updatePasswordResource.getNewPassword(), userToUpdate.getPassword()) || isPasswordMatch)) {

			String message = LoggingUtil.adminAuditInfo("User Password Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"Password updation failed for User : ", usernameVal, BluefinWebPortalConstants.SEPARATOR,
					"New password should be different from your last ", String.valueOf(lastPasswordCount), " passwords.");
			LOGGER.error(message);

			throw new CustomBadRequestException("Your new password should be different from your last "+lastPasswordCount+" passwords");
		}

		List<Object> passwordHistoryAndCount = new ArrayList<>();
		passwordHistoryAndCount.add(passwordHistoryList);
		passwordHistoryAndCount.add(lastPasswordCount);

		return passwordHistoryAndCount;
	}

	public File getUsersReport(String search) throws IOException {
		List<User> result;
		File file;

		String reportPath = propertyDAO.getPropertyValue("USERS_REPORT_PATH");
		LOGGER.debug("ReportPath : {}",reportPath);

		List<String> filterList = getFilterList(search);
		Map<String, String> filterMap = getFilterValues(filterList);
		result = userDAO.findUsersReport(filterList, filterMap);

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");

		file = createFileToPrepareReport(reportPath);
		// initialize FileWriter object
		try (FileWriter fileWriter = new FileWriter(file);
			 CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
			// initialize CSVPrinter object

			// Create CSV file header
			csvFilePrinter.printRecord(FILE_HEADER);
			Integer count = 1;
			LOGGER.debug("Result size : ",result.size());
			// Write a new transaction object list to the CSV file
			for (User user : result) {
				List<String> transactionDataRecord = prepareDataForUsersReport(user,String.valueOf(count));
				csvFilePrinter.printRecord(transactionDataRecord);
				count++;
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}
		return file;
	}

	private List<String> getFilterList(String search) {
		List<String> filterList = new ArrayList<>();

		String[] searchArray = StringUtils.isNotBlank(search) ? search.split("\\$\\$") : null;
		if (searchArray != null) {
			filterList = Arrays.asList(searchArray);
		}

		return filterList;
	}

	private Map<String, String> getFilterValues(List<String> search) {
		Map<String, String> filterMap = new HashMap<>();

		if (search != null && !search.isEmpty()) {
			for (String searchParam : search) {
				checkUser(searchParam, filterMap);
			}
		}

		return filterMap;
	}

	private File createFileToPrepareReport(String reportPath) {

		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			File file = new File(dir, UUID.randomUUID() + ".csv");
			boolean flag;
			flag = file.createNewFile();
			if (flag) {
				LOGGER.info("Report file Created {}", file.getName());
			}
			return file;
		} catch (Exception e) {
			LOGGER.error("Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
		}
	}

	private List<String> prepareDataForUsersReport(User user,String count){
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa");
		List<String> userDataRecord = new ArrayList<>();
		userDataRecord.add(count);
		userDataRecord.add(user.getUsername());
		userDataRecord.add(user.getFirstName());
		userDataRecord.add(user.getLastName());
		userDataRecord.add(user.getRoleName());
		userDataRecord.add(user.getEmail());
		userDataRecord.add(user.getDateCreated().toString());
		userDataRecord.add(user.getDateModified().toString());
		userDataRecord.add(user.getLastLogin().toString());
		userDataRecord.add(user.getLastDatePasswordModified() != null ? user.getLastDatePasswordModified().toString() : "");
		userDataRecord.add(user.getStatus());

		return userDataRecord;
	}
}
