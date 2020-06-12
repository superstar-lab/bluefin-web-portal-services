package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/applicationProperties")
public class ApplicationPropertyLookupController {

	@Autowired
	PropertyService propertyService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationPropertyLookupController.class);

	@ApiOperation(value = "getApplicationProperties", nickname = "getApplicationProperties")
	@GetMapping(produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApplicationProperty.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public List<ApplicationProperty> applicationProperties() {
		LOGGER.debug("application properties endpoint");

		return propertyService.getAllProperty();
	}
	
	@ApiOperation(value = "updateApplicationProperties", nickname = "updateApplicationProperties")
	@PutMapping(consumes = "application/json", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApplicationProperty.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ApplicationProperty updateProperties(@RequestBody ApplicationProperty applicationProperty, @ApiIgnore Authentication authentication) {
		LOGGER.debug("PUT-application properties endpoint");
		
		String logArg1 = "Application Properties Update Request";
		
		String message = LoggingUtil.adminAuditInfo(logArg1, BluefinWebPortalConstants.SEPARATOR, BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		
		if (authentication == null) {
			LOGGER.error(message);
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
		message = LoggingUtil.adminAuditInfo(logArg1, BluefinWebPortalConstants.SEPARATOR, BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), " For the property", BluefinWebPortalConstants.SEPARATOR, applicationProperty.getPropertyName());
		LOGGER.info(message) ;

		if(applicationProperty.getPropertyId()==null) {
			message = LoggingUtil.adminAuditInfo(logArg1, BluefinWebPortalConstants.SEPARATOR, BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication.getName()), BluefinWebPortalConstants.SEPARATOR,"Applicaton id can't be null for update operation");
			LOGGER.error(message);
			throw new CustomException("Applicaton id cann't be null for update operation");
		}
		return propertyService.updateProperty(applicationProperty, authentication.getName());
	}
	
	@ApiOperation(value = "insertApplicationProperties", nickname = "insertApplicationProperties")
	@PostMapping(consumes = "application/json", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApplicationProperty.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ApplicationProperty insertApplicationProperties(@RequestBody ApplicationProperty applicationProperty, @ApiIgnore Authentication authentication) {
		String message = LoggingUtil.adminAuditInfo("Application Property Insertion Request", BluefinWebPortalConstants.SEPARATOR,
				BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
				"Applicaton Property Name : ", applicationProperty.getPropertyName());
		LOGGER.info(message);

		return propertyService.saveApplicationProperty(applicationProperty);
	}
	
	@ApiOperation(value = "deleteApplicationProperties", nickname = "deleteApplicationProperties")
	@DeleteMapping(value = "/{applicationPropertyId}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public String deleteApplicationProperties(@PathVariable String applicationPropertyId, @ApiIgnore Authentication authentication) {
		
		String logArg1 = "Application Properties Deletion Request";
		String name="";
		if(authentication!=null) {
			name = authentication.getName();
		}
		String message = LoggingUtil.adminAuditInfo(logArg1, BluefinWebPortalConstants.SEPARATOR,BluefinWebPortalConstants.REQUESTEDBY, name, BluefinWebPortalConstants.SEPARATOR,"Applicaton Property Id can't be null for delete operation");
		
		if(StringUtils.isBlank(applicationPropertyId)) {
			LOGGER.error(message);
			
			throw new CustomException("Applicaton id cann't be null for delete operation");
		}
		message = LoggingUtil.adminAuditInfo(logArg1, BluefinWebPortalConstants.SEPARATOR, BluefinWebPortalConstants.REQUESTEDBY,name, BluefinWebPortalConstants.SEPARATOR,"Applicaton Property Id : ", applicationPropertyId);
		LOGGER.info(message);
		
		return propertyService.deleteApplicationProperty(applicationPropertyId);
	}
}
