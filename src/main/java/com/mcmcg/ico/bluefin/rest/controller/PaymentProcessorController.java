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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorService;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/payment-processors")
public class PaymentProcessorController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorController.class);

    @Autowired
    private PaymentProcessorService paymentProcessorService;

    @ApiOperation(value = "getPaymentProcessor", nickname = "getPaymentProcessor")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessor getPaymentProcessor(@PathVariable Long id, @ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.info("Getting information with the following id: {}", id);
        return paymentProcessorService.getPaymentProcessorById(id);
    }

    @ApiOperation(value = "getPaymentProcessors", nickname = "getPaymentProcessors")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<PaymentProcessor> getPaymentProcessors(@RequestParam("search") String search,
            @RequestParam(value = "page") Integer page, @RequestParam(value = "size") Integer size,
            @RequestParam(value = "sort", required = false) String sort, @ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.info("Getting information with the following filters: {}", search);
        return paymentProcessorService
                .getPaymentProcessors(QueryDSLUtil.createExpression(search, PaymentProcessor.class), page, size, sort);
    }

    @ApiOperation(value = "createPaymentProcessor", nickname = "createPaymentProcessor")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = PaymentProcessor.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<PaymentProcessor> createPaymentProcessor(
            @Validated @RequestBody PaymentProcessorResource paymentProcessorResource, @ApiIgnore Errors errors,
            @ApiIgnore Authentication authentication) throws Exception {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Creating new payment processor: {}", paymentProcessorResource.getProcessorName());
        return new ResponseEntity<PaymentProcessor>(
                paymentProcessorService.createPaymentProcessor(paymentProcessorResource), HttpStatus.CREATED);
    }

    @ApiOperation(value = "updatePaymentProcessor", nickname = "updatePaymentProcessor")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessor updatePaymentProcessor(@PathVariable Long id, @ApiIgnore Authentication authentication,
            @Validated @RequestBody PaymentProcessorResource paymentProcessorToUpdate, @ApiIgnore Errors errors)
            throws Exception {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.info("Updating Payment Processor {}", paymentProcessorToUpdate);
        return paymentProcessorService.updatePaymentProcessor(id, paymentProcessorToUpdate);
    }

    @ApiOperation(value = "deletePaymentProcessor", nickname = "deletePaymentProcessor")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<PaymentProcessor> deletePaymentProcessor(@PathVariable Long id,
            @ApiIgnore Authentication authentication) throws Exception {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.info("Deleting Payment Processor {}", id);
        paymentProcessorService.deletePaymentProcessor(id);
        LOGGER.info("Payment Processor {} has been deleted.", id);
        return new ResponseEntity<PaymentProcessor>(HttpStatus.OK);
    }
}
