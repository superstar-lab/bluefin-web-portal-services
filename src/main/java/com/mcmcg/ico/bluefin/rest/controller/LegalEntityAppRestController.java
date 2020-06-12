package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.BasicLegalEntityAppResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/legal-entities")
public class LegalEntityAppRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppRestController.class);

    @Autowired
    private LegalEntityAppService legalEntityAppService;
    
    private static final String ACCESS_DENIED = "An authorization token is required to request this resource";
    private static final String AUTH_NULL = "Authentication parameter cannot be NULL Error: {}";

    @ApiOperation(value = "getLegalEntity", nickname = "getLegalEntity")
    @GetMapping(value = "{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp get(@PathVariable Long id) {
        LOGGER.debug("Getting legal entity by id ={}",id);
        return legalEntityAppService.getLegalEntityAppById(id);
    }

    @ApiOperation(value = "getLegalEntities", nickname = "getLegalEntities")
    @GetMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<LegalEntityApp> get(@ApiIgnore Authentication authentication) {
        LOGGER.info("Getting all legal entities");
        if (authentication == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return legalEntityAppService.getLegalEntities(authentication);
    }

    
    @ApiOperation(value = "getActiveLegalEntities", nickname = "getActiveLegalEntities")
	@GetMapping(value="/active", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<LegalEntityApp> getActiveLegalEntities(@ApiIgnore Authentication authentication) {
        LOGGER.info("Getting all legal entities");
        if (authentication == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return legalEntityAppService.getActiveLegalEntities(authentication);
    }
    
    
    
    
    
    
    @ApiOperation(value = "createLegalEntityApp", nickname = "createLegalEntityApp")
    @PostMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<LegalEntityApp> create(
            @Validated @RequestBody BasicLegalEntityAppResource legalEntityResource, @ApiIgnore Errors errors,
            @ApiIgnore Authentication authentication) {
    	final Short activeStatus=legalEntityResource.getIsActive();
    	final Short activeForBatchUpload=legalEntityResource.getIsActiveForBatchUpload();
    	String message = "";
    	String userName = "";
    	try {
    		userName = authentication.getName();
    	}catch(Exception ex) {
    		LOGGER.info(AUTH_NULL , ex.getMessage());
    	}
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
            message = LoggingUtil.adminAuditInfo("Legal Entity Application Creation Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY,userName, BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload), BluefinWebPortalConstants.SEPARATOR,errorDescription);
            LOGGER.error(message);
            
            throw new CustomBadRequestException(errorDescription);
        }
        if(activeStatus==null || activeForBatchUpload==null){
        	message = LoggingUtil.adminAuditInfo("Legal Entity Application create Request:", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload), BluefinWebPortalConstants.SEPARATOR,
        			"Active/In-active status Or Active/In-Active for batch cannot be null.");
        	LOGGER.error(message);
        	throw new CustomBadRequestException("value of Active/In-active status Or value of Active/In-Active for batch upload cannot be null.");
        }
        message = LoggingUtil.adminAuditInfo("Legal Entity Application Creation Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY,userName, BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload));
        LOGGER.info(message);
        
        return new ResponseEntity<>(
                legalEntityAppService.createLegalEntity(legalEntityResource, userName),
                HttpStatus.CREATED);
    }

    @ApiOperation(value = "updateLegalEntityApp", nickname = "updateLegalEntityApp")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @PutMapping(value = "/{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp update(@PathVariable Long id,
            @Validated @RequestBody BasicLegalEntityAppResource legalEntityAppToUpdate, @ApiIgnore Errors errors,
            @ApiIgnore Authentication authentication) {
    	final Short activeStatus=legalEntityAppToUpdate.getIsActive();
    	final Short activeForBatchUpload=legalEntityAppToUpdate.getIsActiveForBatchUpload();
    	String message = "";
    	String userName = "";
    	try {
    		userName = authentication.getName();
    	}catch(Exception ex) {
    		LOGGER.info(AUTH_NULL, ex.getMessage());
    	}
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            message = LoggingUtil.adminAuditInfo("Legal Entity Application Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppToUpdate.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            LOGGER.error(message);
            
            throw new CustomBadRequestException(errorDescription);
        }
        if(activeStatus==null || activeForBatchUpload==null){
        	message = LoggingUtil.adminAuditInfo("Legal Entity Application Update Request:", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppToUpdate.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
        			BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload), BluefinWebPortalConstants.SEPARATOR,
        			"Active/In-active status Or Active/In-Active for batch cannot be null.");
        	LOGGER.error(message);
        	throw new CustomBadRequestException("value of Active/In-active status Or value of Active/In-Active for batch upload cannot be null.");
        }
        message = LoggingUtil.adminAuditInfo("Legal Entity Application Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppToUpdate.getLegalEntityAppName(), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.ACTIVESTATUS, String.valueOf(activeStatus), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.ACTIVEFORBATCHUPLOAD, String.valueOf(activeForBatchUpload));
        LOGGER.info(message);
        
        return legalEntityAppService.updateLegalEntityApp(id, legalEntityAppToUpdate, userName);
    }

    @ApiOperation(value = "deleteLegalEntityApp", nickname = "deleteLegalEntityApp")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @DeleteMapping(value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id, @ApiIgnore Authentication authentication) {
    	String userName = "";
    	try {
    		userName = authentication.getName();
    	}catch(Exception ex) {
    		LOGGER.info(AUTH_NULL , ex.getMessage());
    	}
    	String message = LoggingUtil.adminAuditInfo("Legal Entity App Deletion Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, userName, BluefinWebPortalConstants.SEPARATOR,
        		"Legal Entity Id : ", String.valueOf(id));
        LOGGER.info(message);
        
        legalEntityAppService.deleteLegalEntityApp(id);
        LOGGER.debug("Legal Entity {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "getAllLegalEntities", nickname = "getAllLegalEntities")
    @GetMapping(value = "/all", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<LegalEntityApp> getAllLegalEntity(@ApiIgnore Authentication authentication) {
        LOGGER.info("Fetching all legal entities");
        if (authentication == null) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        return legalEntityAppService.getAllLegalEntities(authentication);
    }
}
