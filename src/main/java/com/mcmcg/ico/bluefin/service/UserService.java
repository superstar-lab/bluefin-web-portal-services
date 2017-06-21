package com.mcmcg.ico.bluefin.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.model.UserPreference;
import com.mcmcg.ico.bluefin.model.UserPreferenceEnum;
import com.mcmcg.ico.bluefin.model.UserRole;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserDAOImpl;
import com.mcmcg.ico.bluefin.repository.UserLegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.UserPreferenceDAO;
import com.mcmcg.ico.bluefin.repository.UserRoleDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.ActivationResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

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

	private static final String REGISTER_USER_EMAIL_SUBJECT = "Bluefin web portal: Register user email";
	private static final String DEACTIVATE_ACCOUNT_EMAIL_SUBJECT = "Bluefin web portal: Deactivated account";

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
		LOGGER.debug("UserService :: getUser() : userRoles size : "+userRoles.size());
		user.setRoles(userRoles);
		List<UserLegalEntityApp> userLegalEntityApps = userLegalEntityAppDAO.findByUserId(user.getUserId());
		LOGGER.debug("UserService :: getUser() : userLegalEntityApps size : "+userLegalEntityApps.size());
		user.setLegalEntities(userLegalEntityApps);
		return user;
	}

	public Iterable<User> getUsers(List<String> search, Integer page, Integer size, String sort) {
		LOGGER.info("Entering to UserService :: getUser(list) ");
		Map<String,String> filterMap = new HashMap<>(7);
		if(search != null && !search.isEmpty()) {
			for(String searchParam:search){
				
				String[] str1 = searchParam.split(":");
				if ("legalEntities".equalsIgnoreCase(str1[0]) || "roles".equalsIgnoreCase(str1[0]) ) {
					str1[1] = str1[1].replace("[", "");
					str1[1] = str1[1].replace("]", "");
					filterMap.put(str1[0], str1[1]);
				} else {
					filterMap.put(str1[0], "%".concat(str1[1]).concat("%"));
				}
			}
		}
		Page<User> result = userDAO.findAllWithDynamicFilter(search, QueryDSLUtil.getPageRequest(page, size, sort),filterMap);
		if (page > result.getTotalPages() && page != 0) {
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		LOGGER.info("exiting from UserService :: getUser(list) ");
		return result;
	}

	/**
	 * Gets the legal entities by user name
	 * 
	 * @param userName
	 * @return list of legal entities owned by the user with the user name given
	 *         by parameter, empty list if user not found
	 */
	public List<LegalEntityApp> getLegalEntitiesByUser(final String username) {
		User user = userDAO.findByUsername(username);
		LOGGER.debug("UserService :: getLegalEntitiesByUser() : user id : "+(user == null ? null : user.getUserId()));
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
			throw new CustomBadRequestException(
					"Unable to create the account, this username already exists: " + username);
		}

		User newUser = userResource.toUser(roleService.getRolesByIds(userResource.getRoles()),
				getLegalEntityAppsByIds(userResource.getLegalEntityApps()));
		newUser.setStatus("NEW");
		newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

		long userId = userDAO.saveUser(newUser);
		LOGGER.debug("UserService :: registerNewUserAccount() : userid : "+userId);
		UserResource newUserResource = new UserResource(getUser(username));

		try{
		//Create/Update User preference of time zone
		newUser.setUserId(userId);
		updaUserPrefernce(newUser);
		}catch(Exception ex){
			LOGGER.error("Error while update user prefrence time zone", ex);
		}
		
		// Send email
		final String link = "/api/users/" + username + "/password";
		final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
		String content = "Welcome to the Bluefin Portal.  Below is your username and a link to create a password. \n\n"
				+ "Username: " + username + "\n\n To create your password, use the link below: \n\n"
				+ propertyService.getPropertyValue("REGISTER_USER_EMAIL_LINK") + "?user=" + username + "&token="
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
			LOGGER.debug("UserService :: getLegalEntityAppsByIds() : LegalEntityApp result size : {}",result.size());
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
		LOGGER.info("UserService :: existUsername()");
		return userDAO.findByUsername(username) == null ? false : true;
	}

	/**
	 * Update the profile information of an already stored user
	 * 
	 * @param username
	 * @param updateUserResource
	 * @return userResource with all the user information
	 * @throws CustomNotFoundException
	 */
	public UserResource updateUserProfile(String username, UpdateUserResource userResource) {
		User user = getUser(username);

		LOGGER.debug("UserService :: updateUserProfile() : user : "+user);
		// Updating fields from existing user
		user.setFirstName(userResource.getFirstName());
		user.setLastName(userResource.getLastName());
		user.setEmail(userResource.getEmail());
		user.setDateUpdated(new DateTime());
		String modifiedBy = null;
		user.setSelectedTimeZone(userResource.getSelectedTimeZone());
		//TODO
		//We are setting empty collectionn object not  to update roles in case of password update
		user.setRoles(Collections.emptyList());
		user.setLegalEntities(Collections.emptyList());
		updaUserPrefernce(user);
		userDAO.updateUser(user, modifiedBy);
		return new UserResource(user);
	}

	private void updaUserPrefernce(User user) {
		long preferenceId = userPreferenceDAO.findPreferenceIdByPreferenceKey(UserPreferenceEnum.USERTIMEZONEPREFRENCE.toString());
		LOGGER.debug("UserService :: updaUserPrefernce() : preferenceId : "+preferenceId);
		/**
		 * fetching UserPreferenceID from UserPreference_Lookup table based on userId & PreferenceID.
		 */
		UserPreference userPreference = userPreferenceDAO.findUserPreferenceIdByPreferenceId(user.getUserId(), preferenceId);
		LOGGER.debug("UserService :: updaUserPrefernce() : userPreference : "+userPreference);
		
		if (userPreference != null && userPreference.getUserPrefeenceID() != null) {
			userPreference.setPreferenceValue(user.getSelectedTimeZone());
			userPreferenceDAO.updateUserTimeZonePreference(userPreference);
		}else{
			UserPreference UserPreferenceToInsert = createUserPreference(preferenceId, user.getUserId(), user.getSelectedTimeZone());
			userPreferenceDAO.insertUserTimeZonePreference(UserPreferenceToInsert);
		}
	}

	private UserPreference createUserPreference(long preferenceId, Long userId, String selectedTimeZone) {
		LOGGER.info("UserService :: createUserPreference() ");
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
	 * @param roles
	 * @return userResource with all the user information
	 * @throws CustomNotFoundException
	 */
	public User updateUserRoles(final String username, final Set<Long> rolesIds) {
		User userToUpdate = getUser(username);

		LOGGER.debug("UserService :: updateUserRoles() : userToUpdate : "+userToUpdate);
		// User wants to clear roles from user
		if (rolesIds.isEmpty()) {
			throw new CustomBadRequestException("User MUST have at least one role assign to him.");
		}

		// Validate and load existing roles
		Map<Long, Role> newMapOfRoles = roleService.getRolesByIds(rolesIds).stream()
				.collect(Collectors.toMap(Role::getRoleId, r -> r));

		LOGGER.debug("UserService :: updateUserRoles() : newMapOfRoles size : "+newMapOfRoles.size());
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
		String modifiedBy = null;
		removeRolesFromUser(rolesToRemove);
		//TODO
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setLegalEntities(Collections.emptyList());
		LOGGER.info("UserService :: updateUserRoles() : ready to update user : ");
		userDAO.updateUser(userToUpdate, modifiedBy);
		return getUser(username);
	}

	private void removeRolesFromUser(Set<Long> rolesToRemove) {
		LOGGER.info("UserService :: removeRolesFromUser() ");
		userRoleDAO.deleteUserRoleById(rolesToRemove);
	}

	/**
	 * Update the legalEntities of an already stored user
	 * 
	 * @param username
	 * @param legalEntities
	 * @return user with all the user information
	 * @throws CustomNotFoundException
	 */
	public User updateUserLegalEntities(final String username, final Set<Long> legalEntityAppsIds) {
		User userToUpdate = getUser(username);

		LOGGER.debug("UserService :: updateUserLegalEntities() : userToUpdate : "+userToUpdate);
		// User wants to clear legal entity apps from user
		if (legalEntityAppsIds.isEmpty()) {
			throw new CustomBadRequestException("User MUST have at least one legal entity assign to him.");
		}

		// Validate and load existing legal entity apps
		Map<Long, LegalEntityApp> newMapOfLegalEntityApps = getLegalEntityAppsByIds(legalEntityAppsIds).stream()
				.collect(Collectors.toMap(LegalEntityApp::getLegalEntityAppId, l -> l));

		LOGGER.debug("UserService :: updateUserLegalEntities() : newMapOfLegalEntityApps size : "+newMapOfLegalEntityApps.size());
		// Temporal list of legal entity apps that we need to keep in the user
		// legal entity app list
		Set<Long> legalEntityAppsToKeep = new HashSet<Long>();
		Set<Long> legalEntityAppsToRemove = new HashSet<Long>();
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
		for (Long legalEntityAppId : newMapOfLegalEntityApps.keySet()) {
			if (!legalEntityAppsToKeep.contains(legalEntityAppId)) {
				userToUpdate.addLegalEntityApp(newMapOfLegalEntityApps.get(legalEntityAppId));
			}
		}
		userToUpdate.setDateUpdated(new DateTime());
		String modifiedBy = null;
		removeLegalEntityFromUser(legalEntityAppsToRemove);
		//TODO
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setRoles(Collections.EMPTY_LIST);
		LOGGER.debug("UserService :: updateUserLegalEntities() : ready to update user");
		userDAO.updateUser(userToUpdate, modifiedBy);
		return getUser(username);
	}

	public void removeLegalEntityFromUser(Collection<Long> legalEntityAppsToRemove) {
		LOGGER.info("UserService :: removeLegalEntityFromUser() ");
		userLegalEntityAppDAO.deleteUserLegalEntityAppById(legalEntityAppsToRemove);
	}

	/**
	 * Validates if the current legal entities of the user that tries to get the
	 * information are valid by checking the values of the request with the ones
	 * owned by the user
	 * 
	 * @param legalEntityIds
	 * @param userName
	 */
	public boolean hasUserPrivilegesOverLegalEntities(Authentication authentication, Set<Long> legalEntitiesToVerify) {
		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return true;
		}
		// Get Legal Entities from user name
		Set<Long> userLegalEntities = getLegalEntitiesByUser(authentication.getName()).stream()
				.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());

		LOGGER.debug("UserService :: hasUserPrivilegesOverLegalEntities() : userLegalEntities size : "+userLegalEntities.size());
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
		LOGGER.debug("UserService :: hasPermissionToManageAllUsers() : authentication : "
				+(authentication == null ? null : (authentication.getAuthorities() == null ? null : authentication.getAuthorities().size())));
		if (authentication != null) {
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				String userAuthority = authority.getAuthority();
				if ("ADMINISTRATIVE".equals(userAuthority) || "MANAGE_ALL_USERS".equals(userAuthority)) {
					hasPermission = true;
				}
				if (hasPermission) {
					break;
				}
			}
		}
		LOGGER.debug("UserService :: hasPermissionToManageAllUsers() : hasPermission : "+hasPermission);
		return hasPermission;
	}

	/**
	 * This method will return true if both users have a common legal entity,
	 * false in other case
	 * 
	 * @param username
	 * @param usernameToUpdate
	 * @return true if the request user has related legal entities with the user
	 *         he wants to CRUD
	 * @throws CustomNotFoundException
	 *             username not found
	 */
	public boolean belongsToSameLegalEntity(Authentication authentication, final String usernameToUpdate) {
		final String username = authentication.getName();
		LOGGER.debug("UserService :: belongsToSameLegalEntity() : username : "+username);
		if (usernameToUpdate.equals(username)) {
			return true;
		}

		// Verify if user that needs to be updated exist
		User userToUpdate = getUser(usernameToUpdate);
		LOGGER.debug("UserService :: belongsToSameLegalEntity() : userToUpdate : "+userToUpdate.getUserId());
		
		if (sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			return true;
		}
		// Get Legal Entities from consultant user
		Set<Long> userLegalEntities = getLegalEntitiesByUser(username).stream()
				.map(userLegalEntityApp -> userLegalEntityApp.getLegalEntityAppId()).collect(Collectors.toSet());
		LOGGER.debug("UserService :: belongsToSameLegalEntity() : userLegalEntities size : "+userLegalEntities.size());
		// Get Legal Entities from user that will be updated
		List<LegalEntityApp> list = new ArrayList<>();
		for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(userToUpdate.getUserId())) {
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

		username = ("me".equals(username) ? tokenUtils.getUsernameFromToken(token) : username);
		LOGGER.debug("UserService :: updateUserPassword() : username : "+username);
		String tokenType = tokenUtils.getTypeFromToken(token);
		LOGGER.debug("UserService :: updateUserPassword() : tokenType : "+tokenType);
		if (username == null || tokenType == null) {
			throw new CustomBadRequestException("An authorization token is required to request this resource");
		}

		User userToUpdate = getUser(username);
		LOGGER.debug("UserService :: updateUserPassword() : userToUpdate : "+userToUpdate.getUserId());

		if ((tokenType.equals(TokenType.AUTHENTICATION.name()) || tokenType.equals(TokenType.APPLICATION.name()))
				&& !isValidOldPassword(updatePasswordResource.getOldPassword(), userToUpdate.getPassword())) {
			throw new CustomBadRequestException("The old password is incorrect.");
		}
		if (tokenType.equals(TokenType.REGISTER_USER.name())) {
			userToUpdate.setStatus("ACTIVE");
		}
		if (tokenType.equals(TokenType.FORGOT_PASSWORD.name()) && userToUpdate.getStatus() == "NEW") {
			userToUpdate.setStatus("ACTIVE");
		}
		userToUpdate.setPassword(passwordEncoder.encode(updatePasswordResource.getNewPassword()));
		userToUpdate.setDateUpdated(new DateTime());
		String modifiedBy = null;
		//TODO
		//We are setting empty collectionn object not  to update roles in case of password update
		userToUpdate.setRoles(Collections.emptyList());
		userToUpdate.setLegalEntities(Collections.emptyList());
		LOGGER.info("UserService :: updateUserPassword() : ready to update user : ");
		userDAO.updateUser(userToUpdate, modifiedBy);
		LOGGER.info("UserService :: updateUserPassword() : ready to find user by id: ");
		return userDAO.findByUserId(userToUpdate.getUserId());
	}

	public void userActivation(ActivationResource activationResource) {
		Boolean activate = activationResource.isActivate();
		LOGGER.debug("UserService :: userActivation() : activate "+activate);
		List<String> notFoundUsernames = new ArrayList<>();

		LOGGER.debug("UserService :: userActivation() : activationResource size : "+(activationResource.getUsernames() == null ? null : activationResource.getUsernames().size()));
		for (String username : activationResource.getUsernames()) {

			if (username == null) {
				throw new CustomBadRequestException("An authorization token is required to request this resource");
			}

			String status = (activate ? "NEW" : "INACTIVE");
			User user = userDAO.findByUsername(username);
			if (user == null) {
				notFoundUsernames.add(username);
			} else {
				activateAccount(user, status);
			}
		}
		if (!notFoundUsernames.isEmpty()) {
			throw new CustomNotFoundException("Unable to find users by usernames provided: " + notFoundUsernames);
		}
	}

	private void activateAccount(User userToUpdate, String status) {
		String username = userToUpdate.getUsername();
		LOGGER.debug("UserService :: activateAccount() : username "+username);
		if (!userToUpdate.getStatus().equalsIgnoreCase( status)) {
			userToUpdate.setStatus(status);
			if ("NEW".equals(status)) {
				userToUpdate.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

				// Send email
				final String link = "/api/users/" + username + "/password";
				final String token = sessionService.generateNewToken(username, TokenType.REGISTER_USER, link);
				String content = "Welcome to the Bluefin Portal.  Below is your username and a link to create a password. \n\n"
						+ "Username: " + username + "\n\n To create your password, use the link below: \n\n"
						+ propertyService.getPropertyValue("REGISTER_USER_EMAIL_LINK") + "?user=" + username + "&token="
						+ token;
				emailService.sendEmail(userToUpdate.getEmail(), REGISTER_USER_EMAIL_SUBJECT, content);
			} else {
				String content = "Your account has been deactivated. \n\n"
						+ "Please feel free to contact your system administratior. \n\n";
				emailService.sendEmail(userToUpdate.getEmail(), DEACTIVATE_ACCOUNT_EMAIL_SUBJECT, content);
			}
			String modifiedBy = null;
			//TODO
			// Why we need to update roles and LE while activating/deactivating user, so make Roles/LE list as empty.[Matloob]
			userToUpdate.setRoles(Collections.emptyList());
			userToUpdate.setLegalEntities(Collections.emptyList());
			
			LOGGER.info("UserService :: activateAccount() : ready to update user ");
			long userId = userDAO.updateUser(userToUpdate, modifiedBy);
			LOGGER.debug("UserService :: activateAccount() : userId "+userId);
			//TOOD.................Why are you calling below operation again, I have commented this [Matloob]
		}
	}

	private boolean isValidOldPassword(final String oldPassword, final String currentUserPassword) {
		LOGGER.info("Entering to UserService :: isValidOldPassword()");
		if (StringUtils.isEmpty(oldPassword)) {
			throw new CustomBadRequestException("oldPassword must not be empty");
		}

		LOGGER.info("Exiting from UserService :: isValidOldPassword()");
		return passwordEncoder.matches(oldPassword, currentUserPassword);
	}
}
