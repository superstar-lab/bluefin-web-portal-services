package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.SessionRequestResource;
import com.mcmcg.ico.bluefin.rest.resource.ThirdPartyAppResource;
import com.mcmcg.ico.bluefin.rest.resource.TokenResponse;
import com.mcmcg.ico.bluefin.rest.resource.TransactionTokenRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationRequest;
import com.mcmcg.ico.bluefin.security.rest.resource.AuthenticationResponse;
import com.mcmcg.ico.bluefin.security.rest.resource.TokenType;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.PropertyService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/session")
public class SessionRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionRestController.class);

    @Autowired
    private PropertyService propertyService;
    @Autowired
    private SessionService sessionService;

    @ApiOperation(value = "loginUser", nickname = "loginUser")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse authentication(@Valid @RequestBody AuthenticationRequest authenticationRequest,
            @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            final String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.debug("Authenticating user: {}", authenticationRequest.getUsername());
        Authentication authentication = sessionService.authenticate(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LOGGER.debug("Generating token for user: {}", authenticationRequest.getUsername());
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
    	LOGGER.info("Inside logoutUser");
        final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));

        if (token == null) {
            throw new CustomBadRequestException("An authorization token is required to request this resource");
        }

        sessionService.deleteSession(token);
        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "refreshAuthanticationToken", nickname = "refreshAuthanticationToken")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public AuthenticationResponse refreshAuthanticationToken(HttpServletRequest request) {
    	LOGGER.info("Inside refreshAuthanticationToken");
        final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
        LOGGER.debug("refreshAuthanticationToken token "+token);
        if (token != null) {
            return sessionService.refreshToken(token);
        }

        throw new CustomBadRequestException("An authorization token is required to request this resource");
    }

    @ApiOperation(value = "Reset password", nickname = "resetPassword")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/recovery/password")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
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
        LOGGER.debug("Password reset request from user: {}", username);
        sessionService.resetPassword(username);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "Register API consumer", nickname = "registerAPIConsumer")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/api-consumer")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public TokenResponse registerApplication(@Valid @RequestBody ThirdPartyAppResource thirdparyApp) {
        LOGGER.debug("Registering and Creating session token for API using username-{} & emailId-{}", thirdparyApp.getUsername(),thirdparyApp.getEmail());

        return sessionService.registerApplication(thirdparyApp);
    }
    
    @ApiOperation(value = "re-generateApplicationOrAPIToken", nickname = "regenerateApplicationOrAPIToken")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json", value = "/api-consumer/{username}")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public TokenResponse generateAPIToken(@PathVariable String username) {
    		LOGGER.debug("Re-Generating/Refreshing API token for user-{}",username);
            return sessionService.generateToken(username,TokenType.APPLICATION);
    }
    
    @ApiOperation(value = "generateTransactionToken", nickname = "generateTransactionToken")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json", value = "/transaction-token/{username}")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public TokenResponse generateTransactionToken(@PathVariable String username) {
    	LOGGER.debug("Generated token to SALE/VOID/REFUND transaction using expirty/life set in DB for user-{}", username);
            return sessionService.generateToken(username,TokenType.TRANSACTION);
    }
    
    @ApiOperation(value = "authanticateTransaction", nickname = "authanticateTransaction")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json",value = "/transaction-token")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = AuthenticationResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Boolean authenticationTransaction(HttpServletRequest request,@ApiIgnore Authentication authentication) {
    	LOGGER.info("Inside authanticateTransaction ");
        final String token = request.getHeader(propertyService.getPropertyValue("TOKEN_HEADER"));
        LOGGER.debug("authanticateTransaction token "+token);
        if(authentication != null && authentication.isAuthenticated() ){
        	// do nothing, that means filter has already validated token and move forward
        	return true;
        }

        throw new CustomBadRequestException("An authorization token is required to request this resource");
    }

}
