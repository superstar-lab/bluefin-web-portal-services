package com.mcmcg.ico.bluefin.rest.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/sessions")
public class SessionRestController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);
    
    @ApiOperation(value = "resetPassword", nickname = "resetPassword")
    @RequestMapping(method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public String resetPassword(Principal principal) throws Exception {
        return "Add implementation** reset password"; 
    }
}
