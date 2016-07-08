package com.mcmcg.ico.bluefin.rest.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.UserService;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;

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

    @ApiOperation(value = "getUser", nickname = "getUser")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public UserResource getUserAccount(@PathVariable String username, @ApiIgnore Authentication authentication)
            throws Exception {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.info("Getting user information: {}", username);
        if (username.equals("me")) {
            username = authentication.getName();
        } else {
            if (!userService.havePermissionToGetOtherUsersInformation(authentication, username)) {
                throw new AccessDeniedException("User doesn't have permission to get information from other users");
            }
        }
        return userService.getUserInfomation(username);
    }

    @ApiOperation(value = "getUsers", nickname = "getUsers")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = User.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<User> getUsers(@RequestParam("search") String search, @RequestParam(value = "page") Integer page,
            @RequestParam(value = "size") Integer size, @RequestParam(value = "sort", required = false) String sort,
            @ApiIgnore Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        String userName = principal.getName();
        // Verifies if the search parameter has allowed
        // legal entities for the consultant user
        search = getVerifiedSearch(userName, search);
        LOGGER.info("Generating report with the following filters: {}", search);
        return userService.getUsers(QueryDSLUtil.createExpression(search, User.class), page, size, sort);
    }

    @ApiOperation(value = "createUser", nickname = "createUser")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = UserResource.class),
            @ApiResponse(code = 404, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<UserResource> registerUserAccount(@Validated @RequestBody RegisterUserResource newUser,
            @ApiIgnore Errors errors, @ApiIgnore Principal principal) throws Exception {
        if (principal == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }
        // Gets the legal entities that will be verified, use of set to avoid
        // duplicated values
        Set<Long> legalEntitiesToVerify = newUser.getLegalEntityApps().stream().collect(Collectors.toSet());
        // Checks if the Legal Entities given are valid according with the
        // LegalEntities owned
        if (!userService.hasUserPrivilegesOverLegalEntities(principal.getName(), legalEntitiesToVerify)) {
            throw new AccessDeniedException(
                    String.format("User doesn't have access to add by legal entity restriction"));
        }

        LOGGER.info("Creating new account for user: {}", newUser.getUsername());
        return new ResponseEntity<UserResource>(userService.registerNewUserAccount(newUser), HttpStatus.CREATED);
    }

    @ApiOperation(value = "updateUserProfile", nickname = "updateUserProfile")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public UserResource updateUserProfile(@PathVariable String username, @ApiIgnore Authentication authentication,
            @Validated @RequestBody UpdateUserResource userToUpdate, @ApiIgnore Errors errors) throws Exception {
        if (authentication == null) {
            throw new CustomBadRequestException("An authorization token is required to request this resource");
        }

        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Updating account for user: {}", username);
        if (username.equals("me")) {
            username = authentication.getName();
        } else {
            if (!userService.havePermissionToGetOtherUsersInformation(authentication, username)) {
                throw new AccessDeniedException("User doesn't have permission to get information from other users");
            }
        }
        return userService.updateUserProfile(username, userToUpdate);
    }

    @ApiOperation(value = "updateUserRoles", nickname = "updateUserRoles")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/roles", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public UserResource updateUserRoles(@PathVariable String username, @RequestBody List<Long> roles) throws Exception {
        LOGGER.info("Updating roles for user: {}", username);
        return userService.updateUserRoles(username, roles);
    }

    @ApiOperation(value = "updateUserLegalEntities", nickname = "updateUserLegalEntities")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}/legal-entities", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UserResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public UserResource updateUserLegalEntities(@PathVariable String username, @RequestBody List<Long> legalEntities)
            throws Exception {
        LOGGER.info("Updating legalEntities for user: {}", username);
        return userService.updateUserLegalEntities(username, legalEntities);
    }

    /**
     * Verifies if the given LE are owned by the consultant user
     * 
     * @param userName
     * @param search
     * @return String with the verified search
     */
    private String getVerifiedSearch(String userName, String search) {
        // Searches for the legal entities of a user its user name
        List<LegalEntityApp> legalEntities = userService.getLegalEntitiesByUser(userName);
        // Validates if the user has access to the given legal entities,
        // exception if does not have access
        return QueryDSLUtil.getValidSearchBasedOnLegalEntitiesById(legalEntities, search);
    }

}
