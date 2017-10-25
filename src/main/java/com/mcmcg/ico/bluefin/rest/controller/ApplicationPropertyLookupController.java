package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.ApplicationProperty;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.PropertyService;

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
	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
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
	@RequestMapping(method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApplicationProperty.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ApplicationProperty updateProperties(@RequestBody ApplicationProperty applicationProperty, @ApiIgnore Authentication authentication) {
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}
		LOGGER.debug("update application properties endpoint ={} ", authentication.getName());

		if(applicationProperty.getPropertyId()==null) {
			throw new CustomException("Applicaton id cann't be null for update operation");
		}
		return propertyService.updateProperty(applicationProperty, authentication.getName());
	}
	
	@ApiOperation(value = "insertApplicationProperties", nickname = "insertApplicationProperties")
	@RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApplicationProperty.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ApplicationProperty insertApplicationProperties(@RequestBody ApplicationProperty applicationProperty) {
		LOGGER.debug("insert application properties endpoint");

		return propertyService.saveApplicationProperty(applicationProperty);
	}
	
	@ApiOperation(value = "deleteApplicationProperties", nickname = "deleteApplicationProperties")
	@RequestMapping(method = RequestMethod.DELETE, value = "/{applicationPropertyId}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public String deleteApplicationProperties(@PathVariable String applicationPropertyId) {
		LOGGER.debug("delete application properties endpoint");
		if(StringUtils.isBlank(applicationPropertyId)) {
			throw new CustomException("Applicaton id cann't be null for delete operation");
		}
		
		return propertyService.deleteApplicationProperty(applicationPropertyId);
	}
}