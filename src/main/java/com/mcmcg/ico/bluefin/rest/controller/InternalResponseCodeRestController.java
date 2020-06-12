package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.InternalResponseCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateInternalCodeResource;
import com.mcmcg.ico.bluefin.service.InternalResponseCodeService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/internal-response-codes")
public class InternalResponseCodeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeRestController.class);

    @Autowired
    private InternalResponseCodeService internalResponseCodeService;

    @ApiOperation(value = "getInternalResponseCodesByTransactionType", nickname = "getInternalResponseCodesByTransactionType")
    @GetMapping( produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<InternalResponseCode> getInternalResponseCodesByTransactionType(
            @RequestParam(value = "transactionType", required = false, defaultValue = "SALE") String transactionType,
            @ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.debug("Getting internal response code list of transactionType ={} ",transactionType);
        return internalResponseCodeService.getInternalResponseCodesByTransactionType(transactionType);
    }

    @ApiOperation(value = "createInternalResponseCodes", nickname = "createInternalResponseCodes")
    @PostMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalResponseCode createInternalResponseCodes(
            @Valid @RequestBody InternalCodeResource internalResponseCodeResource, @ApiIgnore Errors errors,@ApiIgnore Authentication auth) {
    	validateAuthentication(auth);
    	String mesagge = "";
    	String currentLoginUserName = "";
    	try {
    		currentLoginUserName = auth.getName();
    	}catch(Exception ex) {
    		LOGGER.error("createInternalResponseCodes - Authentication object Error: {}", ex.getMessage());
    	}
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            mesagge = LoggingUtil.adminAuditInfo("Response Codes Creation Request", BluefinWebPortalConstants.SEPARATOR, 
            		BluefinWebPortalConstants.REQUESTEDBY,currentLoginUserName, BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            LOGGER.error(mesagge);
            
            throw new CustomBadRequestException(errorDescription);
        }
        
        LOGGER.info(mesagge);
        
        return internalResponseCodeService.createInternalResponseCodes(internalResponseCodeResource, currentLoginUserName);
    }

    @ApiOperation(value = "updateInternalResponseCodes", nickname = "updateInternalResponseCodes")
    @PutMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalResponseCode updateInternalResponseCodes(
            @Valid @RequestBody UpdateInternalCodeResource updateInternalResponseCodeResource,
            @ApiIgnore Errors errors) {
    	String message = "";
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            message = LoggingUtil.adminAuditInfo("Response Codes Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.INTERNALCODEID, String.valueOf(updateInternalResponseCodeResource.getInternalCodeId()), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            LOGGER.error(message);
            throw new CustomBadRequestException(errorDescription);
        }
        message = LoggingUtil.adminAuditInfo("Response Codes Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.INTERNALCODEID, String.valueOf(updateInternalResponseCodeResource.getInternalCodeId()));
        LOGGER.info(message);
        
        return internalResponseCodeService.updateInternalResponseCode(updateInternalResponseCodeResource);
    }

    @ApiOperation(value = "deleteInternalResponseCode", nickname = "deleteInternalResponseCode")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @DeleteMapping(value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
        
    	String message = LoggingUtil.adminAuditInfo("Response Codes Deletion Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.INTERNALCODEID, String.valueOf(id));
        LOGGER.info(message);
        
        internalResponseCodeService.deleteInternalResponseCode(id);
        LOGGER.debug("Internal Response Code {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
    
    private void validateAuthentication(Authentication authentication){
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
	}
}
