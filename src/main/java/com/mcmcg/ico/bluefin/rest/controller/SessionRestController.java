package com.mcmcg.ico.bluefin.rest.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.service.SessionService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/session")
public class SessionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);

    @Value("${bluefin.wp.services.token.header}")
    private String securityTokenHeader;
    @Autowired
    private SessionService sessionService;

    @ApiOperation(value = "loginUser", nickname = "loginUser")
    @RequestMapping(method = RequestMethod.POST)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = AuthenticationResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public ResponseEntity<?> authenticationRequest(@RequestBody AuthenticationRequest authenticationRequest)
            throws AuthenticationException {

        LOGGER.info("Authenticating user: {}", authenticationRequest.getUsername());
        Authentication authentication = this.sessionService.authenticate(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOGGER.info("Generating token for user: {}", authenticationRequest.getUsername());
        AuthenticationResponse response = sessionService.generateToken(authenticationRequest.getUsername());
        return ResponseEntity.ok(response);
    }

    // @ApiOperation(value = "resetPassword", nickname = "resetPassword")
    // @RequestMapping(method = RequestMethod.POST)
    // @ApiResponses(value = { @ApiResponse(code = 200, message = "Success",
    // response = String.class),
    // @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code =
    // 500, message = "Failure") })
    // public String resetPassword(Principal principal, @RequestBody String
    // actionType) throws Exception {
    // return "Add implementation** reset password";
    // }

    @ApiOperation(value = "refreshToken", nickname = "refreshToken")
    @RequestMapping(method = RequestMethod.PUT)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = AuthenticationResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public ResponseEntity<?> authenticationRequest(HttpServletRequest request) {
        String token = request.getHeader(securityTokenHeader);
        AuthenticationResponse newTokenResponse = sessionService.refreshToken(token);
        return ResponseEntity.ok(newTokenResponse);
    }
}
