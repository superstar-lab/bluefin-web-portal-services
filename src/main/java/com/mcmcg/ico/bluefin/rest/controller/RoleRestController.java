package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.rest.resource.RoleResource;
import com.mcmcg.ico.bluefin.service.RoleService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/roles")
public class RoleRestController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleRestController.class);

    @Autowired 
    RoleService roleService;
    
    @ApiOperation(value = "getRoles", nickname = "getRoles")
    @RequestMapping(method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = List.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public List<String> getRoles() throws Exception {
        LOGGER.info("Getting all role list");
        return roleService.getRoles(); 
    }

    @ApiOperation(value = "createRole", nickname = "createRole")
    @RequestMapping(method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String createNewRole(@RequestBody RoleResource role) throws Exception {
        LOGGER.info("Add implementation** create new role");
        return "Add implementation** create new role";
    }

    @ApiOperation(value = "deleteRole", nickname = "deleteRole")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{roleName}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String deleteRole(@PathVariable String roleName) throws Exception {
        LOGGER.info("Add implementation** delete role");
        return "Add implementation** delete role";
    }


}
