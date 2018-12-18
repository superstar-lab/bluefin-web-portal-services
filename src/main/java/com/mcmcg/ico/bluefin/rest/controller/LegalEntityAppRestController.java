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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @ApiOperation(value = "getLegalEntity", nickname = "getLegalEntity")
    @RequestMapping(method = RequestMethod.GET, value = "{id}", produces = "application/json")
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
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
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
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        return legalEntityAppService.getLegalEntities(authentication);
    }

    
   @ApiOperation(value = "getActiveLegalEntities", nickname = "getActiveLegalEntities")
    @RequestMapping(method = RequestMethod.GET,value="/active", produces = "application/json")
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
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        return legalEntityAppService.getActiveLegalEntities(authentication);
    }
    
    
    
    
    
    
    @ApiOperation(value = "createLegalEntityApp", nickname = "createLegalEntityApp")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
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
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Legal Entity App Creation Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName()), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info(LoggingUtil.adminAuditInfo("Legal Entity App Creation Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityResource.getLegalEntityAppName()));
        
        return new ResponseEntity<>(
                legalEntityAppService.createLegalEntity(legalEntityResource, authentication==null ? "":authentication.getName()),
                HttpStatus.CREATED);
    }

    @ApiOperation(value = "updateLegalEntityApp", nickname = "updateLegalEntityApp")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp update(@PathVariable Long id,
            @Validated @RequestBody BasicLegalEntityAppResource legalEntityAppToUpdate, @ApiIgnore Errors errors,
            @ApiIgnore Authentication authentication) {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Legal Entity App Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppToUpdate.getLegalEntityAppName()), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info(LoggingUtil.adminAuditInfo("Legal Entity App Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.LEGALENTITYNAME, legalEntityAppToUpdate.getLegalEntityAppName()));
        
        return legalEntityAppService.updateLegalEntityApp(id, legalEntityAppToUpdate, authentication==null ? "":authentication.getName());
    }

    @ApiOperation(value = "deleteLegalEntityApp", nickname = "deleteLegalEntityApp")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id, @ApiIgnore Authentication authentication) {
    	
        LOGGER.info(LoggingUtil.adminAuditInfo("Legal Entity App Deletion Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
        		"Legal Entity Id : ", String.valueOf(id)));
        
        legalEntityAppService.deleteLegalEntityApp(id);
        LOGGER.debug("Legal Entity {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
    
    @ApiOperation(value = "getAllLegalEntities", nickname = "getAllLegalEntities")
    @RequestMapping(method = RequestMethod.GET, value = "/all", produces = "application/json")
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
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        return legalEntityAppService.getAllLegalEntities(authentication);
    }
}
