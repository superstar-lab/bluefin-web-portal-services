package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.BasicTokenResponse;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.SessionRequestResource;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.service.SessionService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/session")
public class SessionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);

    @Value("${bluefin.wp.services.token.header}")
    private String securityTokenHeader;
    @Autowired
    private SessionService sessionService;

    @ApiOperation(value = "loginUser", nickname = "loginUser")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse authentication(@Valid @RequestBody AuthenticationRequest authenticationRequest,
            @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            final String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Authenticating user: {}", authenticationRequest.getUsername());
        Authentication authentication = sessionService.authenticate(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOGGER.info("Generating token for user: {}", authenticationRequest.getUsername());
        AuthenticationResponse response = sessionService.generateToken(authenticationRequest.getUsername());
        return response;
    }

    @ApiOperation(value = "logoutUser", nickname = "logoutUser")
    @RequestMapping(method = RequestMethod.DELETE, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> logout(HttpServletRequest request) {
        final String token = request.getHeader(securityTokenHeader);

        if (token == null) {
            throw new CustomBadRequestException("An authorization token is required to request this resource");
        }

        sessionService.deleteSession(token);
        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "refreshToken", nickname = "refreshToken")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String token = request.getHeader(securityTokenHeader);

        if (token != null) {
            return sessionService.refreshToken(token);
        }

        throw new CustomBadRequestException("An authorization token is required to request this resource");
    }

    @ApiOperation(value = "Reset password", nickname = "resetPassword")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/recovery/password")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> resetPassword(@Validated @RequestBody SessionRequestResource sessionRequestResource,
            @ApiIgnore Errors errors) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        final String username = sessionRequestResource.getUsername();
        LOGGER.info("Password reset request from user: {}", username);
        sessionService.resetPassword(username);

        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "Register API consumer", nickname = "registerAPIConsumer")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/consumer/{username}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public BasicTokenResponse registerApplication(@PathVariable String username) {
        LOGGER.info("Genereting session token for username: {}", username);

        return sessionService.registerApplication(username);
    }

}
