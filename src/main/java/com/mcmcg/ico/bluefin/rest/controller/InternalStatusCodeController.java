package com.mcmcg.ico.bluefin.rest.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.InternalStatusCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.InternalCodeResource;
import com.mcmcg.ico.bluefin.service.InternalStatusCodeService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/internal-status-codes")
public class InternalStatusCodeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalStatusCodeController.class);

    @Autowired
    private InternalStatusCodeService internalStatusCodeService;

    @ApiOperation(value = "getInternalStatusCodes", nickname = "getInternalStatusCodes")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalStatusCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<InternalStatusCode> getInternalStatusCodes(
            @RequestParam(value = "paymentProcessorId", required = false) Long paymentProcessorId,
            @ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        if (paymentProcessorId != null) {
            LOGGER.info("Getting internal status code list by payment processor");
            return internalStatusCodeService.getInternalStatusCodesByPaymentProcessorId(paymentProcessorId);
        } else {
            LOGGER.info("Getting internal status code list");
            return internalStatusCodeService.getInternalStatusCodes();
        }
    }

    @ApiOperation(value = "upsertInternalStatusCodes", nickname = "upsertInternalStatusCodes")
    @RequestMapping(method = RequestMethod.PUT, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InternalStatusCode.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public InternalStatusCode upsertInternalStatusCodes(
            @Validated @RequestBody InternalCodeResource internalStatusCodeResource, @ApiIgnore Errors errors) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Upserting internal Status code");
        return internalStatusCodeService.upsertInternalStatusCodes(internalStatusCodeResource);
    }

    @ApiOperation(value = "deleteInternalStatusCode", nickname = "deleteInternalStatusCode")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
        LOGGER.info("Deleting Internal Status Code {}", id);
        internalStatusCodeService.deleteInternalStatusCode(id);
        LOGGER.info("Internal Status Code {} has been deleted.", id);

        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }
}