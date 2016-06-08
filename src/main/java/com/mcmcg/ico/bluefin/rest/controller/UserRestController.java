package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomUnauthorizedException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.RegisterUserResource;
import com.mcmcg.ico.bluefin.rest.resource.UserResource;
import com.mcmcg.ico.bluefin.service.UserService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/users")
public class UserRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRestController.class);
    @Autowired
    private UserService userService;

    @ApiOperation(value = "getUser", nickname = "getUser")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = UserResource.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public UserResource getUserAccount(@PathVariable String username, Authentication authentication) throws Exception {
        LOGGER.info("Getting user information: {}", username);
        if (username.equals("me")) {
            username = authentication.getName();
        } else {
            if (!userService.havePermissionToGetOtherUsersInformation(authentication, username)) {
                throw new CustomUnauthorizedException(
                        "User doesn't have permission to get information from other users");
            }
        }
        return userService.getUserInfomation(username);
    }

    @ApiOperation(value = "createUser", nickname = "createUser")
    @RequestMapping(method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public UserResource registerUserAccount(@Validated @RequestBody RegisterUserResource newUser, Errors errors)
            throws Exception {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info("Creating new account for user: {}", newUser.getUsername());
        return userService.registerNewUserAccount(newUser);
    }

    @ApiOperation(value = "updateUser", nickname = "updateUser")
    @RequestMapping(method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String updateUserAccount(@RequestBody UserResource account) throws Exception {
        return "Add implementation** update user account";
    }

    @ApiOperation(value = "assignRole", nickname = "assignRole")
    @RequestMapping(method = RequestMethod.PUT, value = "/{username}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String assignRoleToUser(@PathVariable String username, @RequestBody String role) throws Exception {
        return "Add implementation** assign role to user";
    }

    @ApiOperation(value = "deleteUser", nickname = "deleteUser")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{username}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String deleteUserAccount(@PathVariable String username) throws Exception {
        return "Add implementation** delete user";
    }
}
