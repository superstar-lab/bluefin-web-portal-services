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

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ActivationResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdatePasswordResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.UserService;
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
	private SessionService sessionService;
	@Autowired
	private PropertyService propertyService;

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
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.debug("getUser with username:: service "+username);
		if (username.equals("me") || username.equals(authentication.getName())) {
			username = authentication.getName();
		} else if (!userService.hasPermissionToManageAllUsers(authentication)) {
			throw new AccessDeniedException("User does not have sufficient permissions for this profile.");
		}

		// Checks if the Legal Entities of the consultant user are in the user
		// that will be requested
		if (!userService.belongsToSameLegalEntity(authentication, username)) {
			throw new AccessDeniedException("User does not have access to add by legal entity restriction");
		}

		LOGGER.debug("Getting user information: {}", username);
		return userService.getUserInfomation(username.equals("me") ? authentication.getName() : username);
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
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}

		LOGGER.info("getUser :: service");
		final String userName = authentication.getName();
		LOGGER.debug("getUser :: service : userName : "+userName);
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			// Verifies if the search parameter has allowed
			// legal entities for the consultant user
			search = getVerifiedSearch(userName, search);
		}

		int anyOtherParamsIndex = search.indexOf("&");
		if (anyOtherParamsIndex != -1) {
			if (anyOtherParamsIndex < search.length()) {
				search = search.substring(0, anyOtherParamsIndex);
			}
		}
		String[] searchArray = (search!= null && StringUtils.isNotBlank(search)?search.split("\\$\\$"):null);
		List<String>  filterList=  null;
		if(searchArray!=null)
			filterList = Arrays.asList(searchArray);
		
		LOGGER.debug("Generating report with the following filters: {}", search);
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
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}

		LOGGER.debug("createUser :: service : newUser "+newUser);
		// First checks if all required data is given
		if (errors.hasErrors()) {
			String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("<br /> "));
			throw new CustomBadRequestException(errorDescription);
		}

		// Checks if the Legal Entities given are valid according with the
		// LegalEntities owned
		if (!userService.hasUserPrivilegesOverLegalEntities(authentication, newUser.getLegalEntityApps())) {
			throw new AccessDeniedException(
					String.format("User doesn't have access to add by legal entity restriction"));
		}

		LOGGER.debug("Creating new account for user: {}", newUser.getUsername());
		return new ResponseEntity<UserResource>(userService.registerNewUserAccount(newUser), HttpStatus.CREATED);
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
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.info("updateUserProfile :: service");
		if (username.equals("me") || username.equals(authentication.getName())) {
			username = authentication.getName();
		} else {
			if (!userService.hasPermissionToManageAllUsers(authentication)) {
				throw new AccessDeniedException("User does not have sufficient permissions for this profile.");
			}
		}

		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated
		if (!userService.belongsToSameLegalEntity(authentication, username)) {
			throw new AccessDeniedException("User doesn't have permission to get information from other users");
		}

		if (errors.hasErrors()) {
			String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("<br /> "));
			throw new CustomBadRequestException(errorDescription);
		}

		LOGGER.debug("Updating account for user: {}", username);
		return userService.updateUserProfile(username.equals("me") ? authentication.getName() : username, userToUpdate);
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
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.debug("updateUserRoles with username :: service : roles size : "+roles.size());
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated
		if (!userService.belongsToSameLegalEntity(authentication,
				username.equals("me") ? authentication.getName() : username)) {
			throw new AccessDeniedException("User doesn't have permission to add/remove roles to this user.");
		}
		LOGGER.debug("Updating roles for user: {}", username);

		return new UserResource(
				userService.updateUserRoles(username.equals("me") ? authentication.getName() : username, roles));
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
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}

		LOGGER.info("updateUserLegalEntities :: service : legalEntities size : "+legalEntities.size());
		// Checks if the Legal Entities of the consultant user are in the user
		// that will be updated and checks if the Legal Entities given are valid
		// according with the LegalEntities owned
		if (!userService.hasUserPrivilegesOverLegalEntities(authentication, legalEntities)) {
			throw new AccessDeniedException("User doesn't have permission over the given list of legal entities");
		}

		if (!userService.belongsToSameLegalEntity(authentication,
				username.equals("me") ? authentication.getName() : username)) {
			throw new AccessDeniedException("User doesn't have permission to add/remove legal entities to this user.");
		}

		LOGGER.debug("Updating legalEntities for user: {}", username);
		return new UserResource(userService
				.updateUserLegalEntities(username.equals("me") ? authentication.getName() : username, legalEntities));
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
		if (errors.hasErrors()) {
			final String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
					.collect(Collectors.joining("<br /> "));
			throw new CustomBadRequestException(errorDescription);
		}

		LOGGER.info("updateUserPassword :: service");
		if (username.equals("me") || username.equals(authentication.getName())) {
			username = authentication.getName();
		} else if (!userService.hasPermissionToManageAllUsers(authentication)) {
			throw new AccessDeniedException("User does not have sufficient permissions for this profile.");
		}

		final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
		LOGGER.debug("updateUserPassword :: service : token : "+token);
		if (token != null) {
			userService.updateUserPassword(username, updatePasswordResource, token);
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		}

		throw new CustomBadRequestException("An authorization token is required to request this resource");
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
			throw new AccessDeniedException("User does not have sufficient permissions to perform the operation.");
		}

		LOGGER.info("updateUserActivation :: service");
		final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
		LOGGER.debug("updateUserActivation :: service : token : "+token);
		if (token != null) {
			userService.userActivation(activationResource);
			return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		}

		throw new CustomBadRequestException("An authorization token is required to request this resource");
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
		LOGGER.debug("Inside getVerifiedSearch() : legalEntities size : "+legalEntities.size());
		// Validates if the user has access to the given legal entities,
		// exception if does not have access
		return QueryDSLUtil.getValidSearchBasedOnLegalEntitiesById(legalEntities, search);
	}

}
