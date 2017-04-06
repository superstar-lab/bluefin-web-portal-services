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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.InternalResponseCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.rest.resource.UpdateInternalCodeResource;
import com.mcmcg.ico.bluefin.service.InternalResponseCodeService;

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
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<com.mcmcg.ico.bluefin.model.InternalResponseCode> getInternalResponseCodesByTransactionType(
            @RequestParam(value = "transactionType", required = false, defaultValue = "SALE") String transactionType,
            @ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.info("Getting internal response code list");
        return internalResponseCodeService.getInternalResponseCodesByTransactionType(transactionType);
    }

    @ApiOperation(value = "createInternalResponseCodes", nickname = "createInternalResponseCodes")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public com.mcmcg.ico.bluefin.model.InternalResponseCode createInternalResponseCodes(
            @Valid @RequestBody InternalCodeResource internalResponseCodeResource, @ApiIgnore Errors errors,Authentication auth) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }
        String currentLoginUserName = null;
        if (auth != null) {
        	currentLoginUserName = auth.getName();
        }
        LOGGER.info("Creating internal response code");
        return internalResponseCodeService.createInternalResponseCodes(internalResponseCodeResource, currentLoginUserName);
    }

    @ApiOperation(value = "updateInternalResponseCodes", nickname = "updateInternalResponseCodes")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public com.mcmcg.ico.bluefin.model.InternalResponseCode updateInternalResponseCodes(
            @Valid @RequestBody UpdateInternalCodeResource updateInternalResponseCodeResource,
            @ApiIgnore Errors errors) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Updating internal response code");
        return internalResponseCodeService.updateInternalResponseCode(updateInternalResponseCodeResource);
    }

    @ApiOperation(value = "deleteInternalResponseCode", nickname = "deleteInternalResponseCode")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
        LOGGER.info("Deleting Internal Response Code {}", id);
        internalResponseCodeService.deleteInternalResponseCode(id);
        LOGGER.info("Internal Response Code {} has been deleted.", id);

        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }
}
