package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import com.mcmcg.ico.bluefin.service.RolePermissionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.mcmcg.ico.bluefin.model.Role;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.RoleService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/roles")
public class RoleRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleRestController.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionService rolePermissionService;


    @ApiOperation(value = "getRoleById", nickname = "Get role by id")
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Role.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Role get(@PathVariable Long id) {
        LOGGER.debug("Getting role by id ={} ",id);
        return roleService.getRoleById(id);
    }

    @ApiOperation(value = "getRoles", nickname = "Get roles")
    @GetMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Role.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<Role> get(@ApiIgnore Authentication authentication) {
        LOGGER.info("Getting all roles.");
        return roleService.getRoles(authentication);
    }

    @ApiOperation(value = "Assignment of Permissions to Roles")
    @PutMapping(value = "/assign-permissions", produces = { "application/json" })
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Object update(@RequestParam @Valid long role, @RequestParam @Valid String permissions, @RequestParam @Valid String username) {
        LOGGER.info("WebPortalServices -> SessionRestController -> updating Permissions to Roles: {} by user : {} ", role, username);
        if (!StringUtils.isBlank(permissions) && !StringUtils.isBlank(username)) {
            rolePermissionService.rolesandPermissionsAssignment(role, permissions, username);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return "Permissions or Username cannot be empty";
        }
    }
}
