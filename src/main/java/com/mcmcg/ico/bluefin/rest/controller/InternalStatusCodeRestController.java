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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.InternalStatusCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateInternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.Views;
import com.mcmcg.ico.bluefin.service.InternalStatusCodeService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/internal-status-codes")
public class InternalStatusCodeRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalStatusCodeRestController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private InternalStatusCodeService internalStatusCodeService;

    @ApiOperation(value = "getInternalStatusCodesByTransactionType", nickname = "getInternalStatusCodesByTransactionType")
    @GetMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalStatusCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public String getInternalStatusCodesByTransactionType(
            @RequestParam(value = "transactionType", required = false, defaultValue = "ALL") String transactionType,
            @RequestParam(value = "extended", required = false, defaultValue = "false") Boolean extended,
            @ApiIgnore Authentication authentication) throws JsonProcessingException {
    	LOGGER.debug("Request to fetch internal status code Transaction Type={}",transactionType);
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.info("Getting internal status code list");
        ObjectWriter objectWriter;
        if (Boolean.TRUE.equals(extended)) {
            objectWriter = objectMapper.writerWithView(Views.Extend.class);
        } else {
            objectWriter = objectMapper.writerWithView(Views.Summary.class);
        }
        return objectWriter
                .writeValueAsString(internalStatusCodeService.getInternalStatusCodesByTransactionType(transactionType));
    }

    @ApiOperation(value = "createInternalStatusCodes", nickname = "createInternalStatusCodes")
    @PostMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK", response = InternalStatusCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalStatusCode createInternalStatusCodes(
            @Valid @RequestBody InternalCodeResource internalStatusCodeResource, @ApiIgnore Errors errors,@ApiIgnore Authentication auth) {
    	validateAuthentication(auth);
    	String message = "";
    	String userName = "";
    	try {
    		userName =  auth.getName();
    	}catch(Exception ex) {
    		LOGGER.error("createInternalStatusCodes Authentication object cannot be NULL {}", ex.getMessage());
    	}
        // First checks if all required data is given
        if (errors.hasErrors()) {
        	String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        	message = LoggingUtil.adminAuditInfo("Status Codes Creation Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY,userName, BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
        	LOGGER.error(message);
        	
            throw new CustomBadRequestException(errorDescription);
        }
        message = LoggingUtil.adminAuditInfo("Status Codes Creation Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, userName);
        LOGGER.info(message);
        
        return internalStatusCodeService.createInternalStatusCodes(internalStatusCodeResource,userName);
    }

    @ApiOperation(value = "updateInternalStatusCodes", nickname = "updateInternalStatusCodes")
    @PutMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalStatusCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalStatusCode upsertInternalStatusCodes(
            @Valid @RequestBody UpdateInternalCodeResource updateInternalStatusCodeResource, @ApiIgnore Errors errors,@ApiIgnore Authentication auth) {
    	validateAuthentication(auth);
    	String message= "";
    	String userName = "";
    	try {
    		userName =  auth.getName();
    	}catch(Exception ex) {
    		LOGGER.error("createInternalStatusCodes Authentication object cannot be NULL {}", ex.getMessage());
    	}
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            message= LoggingUtil.adminAuditInfo("Status Codes Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            LOGGER.error(message);
            
            throw new CustomBadRequestException(errorDescription);
        }
        message = LoggingUtil.adminAuditInfo("Status Codes Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, userName);
        LOGGER.info(message);
        
        return internalStatusCodeService.updateInternalStatusCode(updateInternalStatusCodeResource,userName);
    }

    @ApiOperation(value = "deleteInternalStatusCode", nickname = "deleteInternalStatusCode")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @DeleteMapping(value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
    	String message = LoggingUtil.adminAuditInfo("Status Codes Delete Request", BluefinWebPortalConstants.SEPARATOR,
    			"Internal Status Code Id : ", String.valueOf(id));
    	LOGGER.info(message);
    	
        internalStatusCodeService.deleteInternalStatusCode(id);
        LOGGER.debug("Internal Status Code {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
    
   
    @ApiOperation(value = "deleteInternalStatusCode", nickname = "deleteInternalStatusCode")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @GetMapping(value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalStatusCode get(@PathVariable Long id) {
        LOGGER.debug("Fetching Internal Status Code {}", id);
        return internalStatusCodeService.getInternalStatusCode(id);
    }
    
    private void validateAuthentication(Authentication authentication){
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
	}
}
