package com.mcmcg.ico.bluefin.rest.controller;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.InternalResponseCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.InternalResponseCodeResource;
import com.mcmcg.ico.bluefin.service.InternalResponseCodeService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/internal-response-codes")
public class InternalResponseCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalResponseCodeController.class);

    @Autowired
    private InternalResponseCodeService internalResponseCodeService;

    @ApiOperation(value = "getInternalResponseCodes", nickname = "getInternalResponseCodes")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<InternalResponseCode> getInternalResponseCodes(@ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.info("Getting internal response code list");
        return internalResponseCodeService.getInternalResponseCodes();
    }

    @ApiOperation(value = "upsertInternalResponseCodes", nickname = "upsertInternalResponseCodes")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalResponseCode.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalResponseCode upsertInternalResponseCodes(
            @Validated @RequestBody InternalResponseCodeResource internalResponseCodeResource,
            @ApiIgnore Errors errors) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Upserting internal response code");
        return internalResponseCodeService.upsertInternalResponseCodes(internalResponseCodeResource);
    }
}
