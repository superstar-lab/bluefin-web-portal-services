package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorRuleResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRuleService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/payment-processor-rules")
public class PaymentProcessorRuleRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleRestController.class);

    @Autowired
    private PaymentProcessorRuleService paymentProcessorRuleService;

    @ApiOperation(value = "Get payment processor rule by id", nickname = "getPaymentProcessorRule")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRule.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public com.mcmcg.ico.bluefin.model.PaymentProcessorRule get(@PathVariable Long id) {
        LOGGER.debug("Getting information with the following id: {}", id);

        return paymentProcessorRuleService.getPaymentProcessorRule(id);
    }

    @ApiOperation(value = "Get payment processor rules", nickname = "getPaymentProcessorRules")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRule.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<PaymentProcessorRule> get() {
        LOGGER.info("Getting information with the following filters: {}");
        return paymentProcessorRuleService.getPaymentProcessorRules();
    }

    @ApiOperation(value = "Get payment processor rule transaction types", nickname = "getPaymentProcessorTransactionTypes")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/transaction-types")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRule.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<CardType> getTransactionTypes() {
        LOGGER.info("Getting transaction types: {}");
        return paymentProcessorRuleService.getTransactionTypes();
    }

    @ApiOperation(value = "Create payment processor rule", nickname = "createPaymentProcessorRule")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = PaymentProcessorRule.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<com.mcmcg.ico.bluefin.model.PaymentProcessorRule> create(
            @Validated @RequestBody PaymentProcessorRuleResource paymentProcessorRuleResource,
            @ApiIgnore Errors errors) {
        // First checks if all required fields are set
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.debug("Creating new payment processor rule: {}", paymentProcessorRuleResource);
        return new ResponseEntity<com.mcmcg.ico.bluefin.model.PaymentProcessorRule>(paymentProcessorRuleService.createPaymentProcessorRule(
                paymentProcessorRuleResource.getPaymentProcessorId().longValue(),
                paymentProcessorRuleResource.toPaymentProcessorRule()), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Update payment processor rule", nickname = "updatePaymentProcessor")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRule.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public com.mcmcg.ico.bluefin.model.PaymentProcessorRule update(@PathVariable Long id,
            @Validated @RequestBody PaymentProcessorRuleResource paymentProcessorRuleResource,
            @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            throw new CustomBadRequestException(errorDescription);
        }

        LOGGER.debug("Updating payment processor rule: {}", paymentProcessorRuleResource);
        return paymentProcessorRuleService.updatePaymentProcessorRule(
                paymentProcessorRuleResource.toPaymentProcessorRule(id),
                paymentProcessorRuleResource.getPaymentProcessorId().longValue());
    }

    @ApiOperation(value = "Delete payment processor rule", nickname = "deletePaymentProcessorRule")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
        LOGGER.debug("Deleting payment processor rule {}", id);
        paymentProcessorRuleService.delete(id);
        LOGGER.debug("Payment processor rule {} has been deleted.", id);

        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }
}
