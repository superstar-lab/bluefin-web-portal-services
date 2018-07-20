package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.mcmcg.ico.bluefin.model.CardType;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRule;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorRuleResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRuleService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

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
    public PaymentProcessorRule get(@PathVariable Long id) {
        LOGGER.debug("Getting payment processor rule with the following id: {}", id);
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
        LOGGER.info("Getting all payment processor rules");
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
        LOGGER.info("Getting all transaction types");
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
    public ResponseEntity<PaymentProcessorRule> create(
            @Validated @RequestBody PaymentProcessorRuleResource paymentProcessorRuleResource,
            @ApiIgnore Errors errors, @ApiIgnore Authentication authentication) {
        // First checks if all required fields are set
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
            		"Payment Processor Id : ", String.valueOf(paymentProcessorRuleResource.getPaymentProcessorId()), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription));
            
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Rule Creation Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
        		"Payment Processor Id : ", String.valueOf(paymentProcessorRuleResource.getPaymentProcessorId())));
        
        return new ResponseEntity<>(paymentProcessorRuleService.createPaymentProcessorRule(
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
    public PaymentProcessorRule update(@PathVariable Long id,
            @Validated @RequestBody PaymentProcessorRuleResource paymentProcessorRuleResource,
            @ApiIgnore Errors errors, @ApiIgnore Authentication authentication) {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Rule Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
            		"Payment Processor Rule Id : ", String.valueOf(id), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription));
            
            throw new CustomBadRequestException(errorDescription);
        }
        
        LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Rule Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
        		"Payment Processor Rule Id : ", String.valueOf(id)));
        
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
    public ResponseEntity<String> delete(@PathVariable Long id, @ApiIgnore Authentication authentication) {
    	LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Rule Delete Request", BluefinWebPortalConstants.SEPARATOR,
    			BluefinWebPortalConstants.REQUESTEDBY, String.valueOf(authentication==null ? "":authentication.getName()), BluefinWebPortalConstants.SEPARATOR,
    			"Payment Processor Rule Id : ", String.valueOf(id)));
    	
        paymentProcessorRuleService.delete(id);
        LOGGER.debug("Payment processor rule {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
}
