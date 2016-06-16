package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.service.SessionService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/rest/bluefin/session")
public class SessionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);

    @Value("${bluefin.wp.services.token.header}")
    private String securityTokenHeader;
    @Autowired
    private SessionService sessionService;

    @ApiOperation(value = "loginUser", nickname = "loginUser")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = AuthenticationResponse.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse authenticationRequest(@Valid @RequestBody AuthenticationRequest authenticationRequest,
            @ApiIgnore Errors errors) throws AuthenticationException {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Authenticating user: {}", authenticationRequest.getUsername());
        Authentication authentication = this.sessionService.authenticate(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOGGER.info("Generating token for user: {}", authenticationRequest.getUsername());
        AuthenticationResponse response = sessionService.generateToken(authenticationRequest.getUsername());
        return response;
    }

    @ApiOperation(value = "refreshToken", nickname = "refreshToken")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = AuthenticationResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse refreshToken(HttpServletRequest request) throws Exception {
        String token = request.getHeader(securityTokenHeader);
        if (token != null) {
            return sessionService.refreshToken(token);
        }

        throw new CustomBadRequestException("An authorization token is required to request this resource");
    }
}
