package com.mcmcg.ico.bluefin.rest.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ActivationResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.TokenUtils;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.UserService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/users")
public class UserRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);
	@Autowired
	private UserService userService;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private TokenUtils tokenUtils;
	
	@ApiOperation(value = "getUser", nickname = "getUser")
	@RequestMapping(method = RequestMethod.GET, value = "/{username:.*}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public UserResource get(@PathVariable String username, @ApiIgnore Authentication authentication) {
		validateAuthentication(authentication);
		LOGGER.debug("service ={}",username);
		String usernameValue="";
		if ("me".equals(username) || username.equals(authentication.getName())) {
			usernameValue = authentication.getName();
		} else if (!userService.hasPermissionToManageAllUsers(authentication)) {
			throw new AccessDeniedException(BluefinWebPortalConstants.USERINSUFFICIENTPERMISSIONMSG);
		}
		if (usernameValue != null && usernameValue.isEmpty()) {
			usernameValue = username;
		}
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be requested
		validateUserLegalEntity(authentication,usernameValue);

		LOGGER.debug("Getting user information: {}", usernameValue);
		return userService.getUserInfomation("me".equals(usernameValue) ? authentication.getName() : usernameValue);
	}

	@ApiOperation(value = "getUsers", nickname = "getUsers")
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = User.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public Iterable<User> get(@RequestParam("search") String search, @RequestParam(value = "page") Integer page,
			@RequestParam(value = "size") Integer size, @RequestParam(value = "sort", required = false) String sort,
			@ApiIgnore Authentication authentication) {
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}

		LOGGER.info("get User  service");
		final String userName = authentication.getName();
		LOGGER.debug("userName ={} ",userName);
		String searchValue;
		//if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
		if (!userService.hasPermissionToManageAllUsers(authentication)) {
			// Verifies if the search parameter has allowed
			// legal entities for the consultant user
			searchValue = getVerifiedSearch(userName, search);
		} else {
			searchValue = search;
		}

		int anyOtherParamsIndex = searchValue.indexOf("&");
		if (anyOtherParamsIndex != -1 && anyOtherParamsIndex < searchValue.length()) {
			searchValue = searchValue.substring(0, anyOtherParamsIndex);
		}
		String[] searchArray = searchValue!= null && StringUtils.isNotBlank(searchValue)?searchValue.split("\\$\\$"):null;
		List<String>  filterList=  null;
		if(searchArray!=null)
			filterList = Arrays.asList(searchArray);
		
		LOGGER.debug("Generating report with the following filters: {}", searchValue);
		return userService.getUsers(filterList, page, size, sort);
	}

	@ApiOperation(value = "createUser", nickname = "createUser")
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ResponseStatus(HttpStatus.CREATED)
	@ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = UserResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<UserResource> create(@Valid @RequestBody RegisterUserResource newUser,
			@ApiIgnore Errors errors, @ApiIgnore Authentication authentication) {
		validateAuthentication(authentication);

		LOGGER.debug("newUser ={}",newUser);
		// First checks if all required data is given
		if (errors.hasErrors()) {
			String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("<br /> "));
			
			LOGGER.error(LoggingUtil.adminAuditInfo("User Creation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDFOR, newUser.getUsername(), BluefinWebPortalConstants.SEPARATOR,
					errorDescription));
			
			throw new CustomBadRequestException(errorDescription);
		}

		// Checks if the Legal Entities given are valid according with the
		// LegalEntities owned
		hasUserPrivilegesOverLegalEntities(authentication,newUser.getLegalEntityApps());

		LOGGER.info(LoggingUtil.adminAuditInfo("User Creation Request", BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getPrincipal()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDFOR, newUser.getUsername()));
		LOGGER.debug("Creating new account for user: {}", newUser.getUsername());
		return new ResponseEntity<>(userService.registerNewUserAccount(newUser), HttpStatus.CREATED);
	}

	@ApiOperation(value = "updateUserProfile", nickname = "updateUserProfile")
	@RequestMapping(method = RequestMethod.PUT, value = "/{username:.*}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public UserResource updateUserProfile(@PathVariable String username, @ApiIgnore Authentication authentication,
			@Valid @RequestBody UpdateUserResource userToUpdate, @ApiIgnore Errors errors) {
		validateAuthentication(authentication);
		
		LOGGER.info("update User Profile service");
		String usernameValue="";
		if ("me".equals(username) || username.equals(authentication.getName())) {
			usernameValue = authentication.getName();
		} else {
			if (!userService.hasPermissionToManageAllUsers(authentication)) {
				LOGGER.error(LoggingUtil.adminAuditInfo("User Profile Updation Request", BluefinWebPortalConstants.SEPARATOR,
						"User : ", String.valueOf(authentication.getName()), " does not have sufficient permissions for this profile."));
				throw new AccessDeniedException(BluefinWebPortalConstants.USERINSUFFICIENTPERMISSIONMSG);
			}
		}
		if (usernameValue != null && usernameValue.isEmpty()) {
			usernameValue = username;
		}	
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated
		try {
			validateUserLegalEntity(authentication, usernameValue);
		} catch(AccessDeniedException e) {
			
			LOGGER.error(LoggingUtil.adminAuditInfo("User Profile Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", String.valueOf(authentication.getName()), " does not have access to add by legal entity restriction."));
			
			throw new AccessDeniedException("User does not have access to add by legal entity restriction");
		}
		validateErrors(errors);
		LOGGER.info(LoggingUtil.adminAuditInfo("User Profile Updation Request", BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDFOR, usernameValue));
		LOGGER.debug("Updating account for user: {}", usernameValue);
		return userService.updateUserProfile("me".equals(usernameValue) ? authentication.getName() : usernameValue, userToUpdate, authentication.getName());
	}

	@ApiOperation(value = "updateUserRoles", nickname = "updateUserRoles")
	@RequestMapping(method = RequestMethod.PUT, value = "/{username}/roles", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public UserResource updateUserRoles(@PathVariable String username, @RequestBody Set<Long> roles,
			@ApiIgnore Authentication authentication) {
		if (authentication == null) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Roles Updation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG));
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
		LOGGER.debug("roles size ={}",roles.size());
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated
		if (!userService.belongsToSameLegalEntity(authentication,
				"me".equals(username) ? authentication.getName() : username)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Profile Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", String.valueOf(authentication.getName()), " doesn't have permission to add/remove roles to this user."));
			throw new AccessDeniedException("User doesn't have permission to add/remove roles to this user.");
		}
		LOGGER.debug("Updating roles for user: {}", username);

		LOGGER.info(LoggingUtil.adminAuditInfo("User Roles Updation Request", BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDFOR, username));
		return new UserResource(
				userService.updateUserRoles("me".equals(username) ? authentication.getName() : username, roles, authentication.getName()));
	}

	@ApiOperation(value = "updateUserLegalEntities", nickname = "updateUserLegalEntities")
	@RequestMapping(method = RequestMethod.PUT, value = "/{username}/legal-entities", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public UserResource updateUserLegalEntities(@PathVariable String username, @RequestBody Set<Long> legalEntities,
			@ApiIgnore Authentication authentication) {
		if (authentication == null) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Legal Entities Updation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG));
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}

		LOGGER.info("legalEntities size ={} ",legalEntities.size());
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated and checks if the Legal Entities given are valid
		// according with the LegalEntities owned
		if (!userService.hasUserPrivilegesOverLegalEntities(authentication, legalEntities)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Legal Entities Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User doesn't have permission over the given list of legal entities"));
			throw new AccessDeniedException("User doesn't have permission over the given list of legal entities");
		}

		if (!userService.belongsToSameLegalEntity(authentication,
				"me".equals(username) ? authentication.getName() : username)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Legal Entities Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", String.valueOf(authentication.getName()), " doesn't have permission to add/remove legal entities to this user."));
			throw new AccessDeniedException("User doesn't have permission to add/remove legal entities to this user.");
		}

		LOGGER.info(LoggingUtil.adminAuditInfo("User Legal Entities Updation Request", BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDFOR, username));
		LOGGER.debug("Updating legalEntities for user: {}", username);
		return new UserResource(userService
				.updateUserLegalEntities("me".equals(username) ? authentication.getName() : username, legalEntities, authentication.getName()));
	}

	@ApiOperation(value = "updateUserPassword", nickname = "updateUserPassword")
	@RequestMapping(method = RequestMethod.PUT, value = "/{username}/password", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> updateUserPassword(@PathVariable String username,
			@Valid @RequestBody UpdatePasswordResource updatePasswordResource, @ApiIgnore Errors errors,
			HttpServletRequest request, @ApiIgnore Authentication authentication) {
		validateErrors(errors);
		
		LOGGER.info("update User Password service");
		String usernameValue="";
		if ("me".equals(username) || username.equals(authentication.getName())) {
			usernameValue = authentication.getName();
		} else if (!userService.hasPermissionToManageAllUsers(authentication)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Password Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"Password updation failed for User : ", String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.USERINSUFFICIENTPERMISSIONMSG));
			throw new AccessDeniedException(BluefinWebPortalConstants.USERINSUFFICIENTPERMISSIONMSG);
		}
		if(usernameValue != null && usernameValue.isEmpty()) {
			usernameValue = username;
		}
		validatePasswordCriteria(usernameValue, updatePasswordResource.getNewPassword());
		final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
		LOGGER.debug("token ={} ",token);
		if (token != null) {
			LOGGER.info(LoggingUtil.adminAuditInfo("User Password Updation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDFOR, username));
			userService.updateUserPassword(usernameValue, updatePasswordResource, token);
			User user = userService.findByUsername(usernameValue);
			if ( user != null ) {
				// After activation of user marked token as black list token
				tokenUtils.sendTokenToBlacklist(token, usernameValue);
			}
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		LOGGER.error(LoggingUtil.adminAuditInfo("User Password Updation Request", BluefinWebPortalConstants.SEPARATOR,
				"Password updation failed for User : ", String.valueOf(authentication.getPrincipal()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG));
		throw new CustomBadRequestException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
	}

	@ApiOperation(value = "updateUserActivation", nickname = "updateUserActivation")
	@RequestMapping(method = RequestMethod.PUT, value = "/status", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> updateUserActivation(@Valid @RequestBody ActivationResource activationResource,
			HttpServletRequest request, @ApiIgnore Authentication authentication) {

		if (!userService.hasPermissionToManageAllUsers(authentication)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Status Updation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", String.valueOf(authentication.getName()), " does not have sufficient permissions for this profile."));
			throw new AccessDeniedException("User does not have sufficient permissions to perform the operation.");
		}

		LOGGER.info("update User Activation service:");
		final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
		LOGGER.debug("token: ={} ",token);
		if (token != null) {
			String users = "";
			for(String userName : activationResource.getUsernames()) {
				if(StringUtils.isNotEmpty(users)) {
					users = users + "," +userName;
				}
				else {
					users = userName;
				}
			}
			LOGGER.info(LoggingUtil.adminAuditInfo("User Status Updation Request", BluefinWebPortalConstants.SEPARATOR,
					BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()),
					BluefinWebPortalConstants.SEPARATOR, BluefinWebPortalConstants.REQUESTEDFOR, users));
			userService.userActivation(activationResource, String.valueOf(authentication.getName()));
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		LOGGER.error(LoggingUtil.adminAuditInfo("User Status Updation Request", BluefinWebPortalConstants.SEPARATOR,
				"Status Updation Request failed for User : ", String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG));
		throw new CustomBadRequestException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
	}

	/**
	 * Verifies if the given LE are owned by the consultant user
	 * 
	 * @param userName
	 * @param search
	 * @return String with the verified search
	 */
	private String getVerifiedSearch(String userName, String search) {
		LOGGER.info("Inside getVerifiedSearch()");
		// Searches for the legal entities of a user its user name
		List<LegalEntityApp> legalEntities = userService.getLegalEntitiesByUser(userName);
		LOGGER.debug("legalEntities size: ={} ",legalEntities.size());
		// Validates if the user has access to the given legal entities,
		// exception if does not have access
		return QueryDSLUtil.getValidSearchBasedOnLegalEntitiesById(legalEntities, search);
	}
	
	private void validateAuthentication(Authentication authentication){
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
	}
	
	private void validateUserLegalEntity(Authentication authentication,String usernameValue){
		if (!userService.belongsToSameLegalEntity(authentication, usernameValue)) {
			throw new AccessDeniedException("User does not have access to add by legal entity restriction");
		}
	}
	
	private void hasUserPrivilegesOverLegalEntities(Authentication authentication,Set<Long> legalEntityApps){ 
		if (!userService.hasUserPrivilegesOverLegalEntities(authentication, legalEntityApps)) {
			LOGGER.error(LoggingUtil.adminAuditInfo("User Creation Request", BluefinWebPortalConstants.SEPARATOR,
					"User : ", String.valueOf(authentication.getName()), " doesn't have access to add by legal entity restriction."));
			throw new AccessDeniedException("User doesn't have access to add by legal entity restriction");
		}
	}
	
	private void validateErrors(Errors errors){
		if (errors.hasErrors()) {
			final String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("<br /> "));
			throw new CustomBadRequestException(errorDescription);
		}
	}
	
	private void validatePasswordCriteria(String userName, String password){
		//As per Dheeraj code review suggestion
		if(StringUtils.isBlank(userName)) {
			throw new CustomBadRequestException("UserName can not be null");	
		}
		if(StringUtils.isBlank(password)) {
			throw new CustomBadRequestException("Password can not null");	
		}
		int passwordLength = password.length();
		String passwordTrimVal = password.trim();
		int passwordTrimLength = passwordTrimVal.trim().length();
		if(passwordLength!=passwordTrimLength || StringUtils.containsWhitespace(passwordTrimVal.trim())) {
			throw new CustomBadRequestException("Password must not contain space");
		}
		if(password.length()<8 || password.length()>16) {
			throw new CustomBadRequestException("Password must be between 8 to 16 characters in length");
		}
		if(userName.equals(password)) {
			throw new CustomBadRequestException("username and password must not be same");
		}
	}
}
