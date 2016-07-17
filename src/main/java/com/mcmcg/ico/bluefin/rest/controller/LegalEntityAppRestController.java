package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;
import java.util.Set;
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

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.BasicLegalEntityAppResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorMerchantResource;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

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
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp getLegalEntity(@PathVariable Long id) {
        LOGGER.info("Getting legal entity by id");
        return legalEntityAppService.getLegalEntityAppById(id);
    }

    @ApiOperation(value = "getLegalEntities", nickname = "getLegalEntities")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<LegalEntityApp> getLegalEntities(@ApiIgnore Authentication authentication) {
        LOGGER.info("Getting all legal entities");
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        return legalEntityAppService.getLegalEntities(authentication.getName());
    }

    @ApiOperation(value = "createLegalEntityApp", nickname = "createLegalEntityApp")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<LegalEntityApp> createLegalEntityApp(
            @Validated @RequestBody BasicLegalEntityAppResource legalEntityResource, @ApiIgnore Errors errors,
            @ApiIgnore Authentication authentication) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Creating new legal entity: {}", legalEntityResource.getLegalEntityAppName());
        return new ResponseEntity<LegalEntityApp>(legalEntityAppService.createLegalEntity(legalEntityResource),
                HttpStatus.CREATED);
    }

    @ApiOperation(value = "updateLegalEntityApp", nickname = "updateLegalEntityApp")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp updateLegalEntityApp(@PathVariable Long id,
            @Validated @RequestBody BasicLegalEntityAppResource legalEntityAppToUpdate, @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Updating Legal Entity {}", legalEntityAppToUpdate);
        return legalEntityAppService.updateLegalEntityApp(id, legalEntityAppToUpdate);
    }

    @ApiOperation(value = "updateLegalEntityAppPaymentProcessorMerchants", nickname = "updateLegalEntityAppPaymentProcessorMerchants")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/payment-processor-merchants", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public LegalEntityApp updateLegalEntityAppPaymentProcessorMerchants(@PathVariable Long id,
            @Validated @RequestBody Set<PaymentProcessorMerchantResource> paymentProcessorMerchants,
            @ApiIgnore Errors errors) {
        LOGGER.info("Updating payment processors merchants = [{}] from legal entity app = [{}]",
                paymentProcessorMerchants, id);

        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        return legalEntityAppService.updateLegalEntityAppPaymentProcessors(id, paymentProcessorMerchants);
    }

    @ApiOperation(value = "deleteLegalEntityApp", nickname = "deleteLegalEntityApp")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<LegalEntityApp> deleteLegalEntityApp(@PathVariable Long id) {
        LOGGER.info("Deleting Payment Processor {}", id);
        legalEntityAppService.deleteLegalEntityApp(id);
        LOGGER.info("Legal Entity {} has been deleted.", id);

        return new ResponseEntity<LegalEntityApp>(HttpStatus.OK);
    }
}
