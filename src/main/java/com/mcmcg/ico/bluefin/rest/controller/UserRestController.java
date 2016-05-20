package com.mcmcg.ico.bluefin.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.jpa.RoleRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.AccountResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/users")
public class UserRestController {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;
    
    @ApiOperation(value = "getUser", nickname = "getUser")
    @RequestMapping(method = RequestMethod.GET, value = "/{username}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String getUserAccount(@PathVariable String username) throws Exception {
        return "Add implementation** get user account"; 
    }
    
    @ApiOperation(value = "getUsers", nickname = "getUsers")
    @RequestMapping(method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String getUserAccounts() throws Exception {
        return "Add implementation** get user accounts"; 
    }

    @ApiOperation(value = "createUser", nickname = "createUser")
    @RequestMapping(method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String registerUserAccount(@RequestBody AccountResource account) throws Exception {
        return "Add implementation** register user account";
    }

    @ApiOperation(value = "updateUser", nickname = "updateUser")
    @RequestMapping(method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String updateUserAccount(@RequestBody AccountResource account) throws Exception {
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
