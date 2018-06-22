package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;
import java.util.Set;
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

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.resource.BasicPaymentProcessorResource;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorMerchantResource;
import com.mcmcg.ico.bluefin.rest.resource.PaymentProcessorStatusResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorService;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/payment-processors")
public class PaymentProcessorRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRestController.class);

    @Autowired
    private PaymentProcessorService paymentProcessorService;

    @ApiOperation(value = "getPaymentProcessor", nickname = "getPaymentProcessor")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessor get(@PathVariable Long id) {
        LOGGER.debug("Getting information with the following id: {}", id);
        return paymentProcessorService.getPaymentProcessorById(id);
    }

    @ApiOperation(value = "getPaymentProcessors", nickname = "getPaymentProcessors")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<PaymentProcessor> get() {
        LOGGER.info("Getting payment processors");
        return paymentProcessorService.getPaymentProcessors();
    }

    @ApiOperation(value = "getPaymentProcessorStatus", nickname = "getPaymentProcessorStatus")
    @RequestMapping(method = RequestMethod.GET, value = "/{id}/status", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessorStatusResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessorStatusResource getStatus(@PathVariable Long id) {
        LOGGER.debug("Getting Payment Processor status with the following id: {}", id);
        return paymentProcessorService.getPaymentProcessorStatusById(id);
    }

    @ApiOperation(value = "createPaymentProcessor", nickname = "createPaymentProcessor")
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created", response = PaymentProcessor.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<PaymentProcessor> create(
            @Validated @RequestBody BasicPaymentProcessorResource paymentProcessorResource, @ApiIgnore Errors errors) {
        // First checks if all required data is given
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Creation Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, paymentProcessorResource.getLastModifiedBy(), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.PAYMENTPROCESSORNAME, paymentProcessorResource.getProcessorName()),
            		errorDescription);
            
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Creation Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, paymentProcessorResource.getLastModifiedBy(), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.PAYMENTPROCESSORNAME, paymentProcessorResource.getProcessorName()));
        
        return new ResponseEntity<>(
                paymentProcessorService.createPaymentProcessor(paymentProcessorResource), HttpStatus.CREATED);
    }

    @ApiOperation(value = "updatePaymentProcessor", nickname = "updatePaymentProcessor")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessor update(@PathVariable Long id,
            @Validated @RequestBody BasicPaymentProcessorResource paymentProcessorToUpdate, @ApiIgnore Errors errors) {
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Update Request", BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.REQUESTEDBY, paymentProcessorToUpdate.getLastModifiedBy(), BluefinWebPortalConstants.SEPARATOR,
            		BluefinWebPortalConstants.PAYMENTPROCESSORNAME, paymentProcessorToUpdate.getProcessorName()), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            
            throw new CustomBadRequestException(errorDescription);
        }
        LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Update Request", BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.REQUESTEDBY, paymentProcessorToUpdate.getLastModifiedBy(), BluefinWebPortalConstants.SEPARATOR,
        		BluefinWebPortalConstants.PAYMENTPROCESSORNAME, paymentProcessorToUpdate.getProcessorName()));
        
        return paymentProcessorService.updatePaymentProcessor(id, paymentProcessorToUpdate);
    }

    @ApiOperation(value = "Update payment processor merchants from payment processor", nickname = "updatePaymentProcessorMerchantsFromPaymentProcessor")
    @RequestMapping(method = RequestMethod.PUT, value = "/{id}/payment-processor-merchants", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessor.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessor updatePaymentProcessorMerchants(@PathVariable Long id,
            @Validated @RequestBody Set<PaymentProcessorMerchantResource> paymentProcessorMerchants,
            @ApiIgnore Errors errors) {
    	LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Merchants Update Request", BluefinWebPortalConstants.SEPARATOR,
    			"Payment Processor Id : ", String.valueOf(id)));
    	
        if (errors.hasErrors()) {
            String errorDescription = errors.getFieldErrors().stream().map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            
            LOGGER.error(LoggingUtil.adminAuditInfo("Payment Processor Merchants Update Request", BluefinWebPortalConstants.SEPARATOR,
        			"Payment Processor Id : ", String.valueOf(id)), BluefinWebPortalConstants.SEPARATOR,
            		errorDescription);
            
            throw new CustomBadRequestException(errorDescription);
        }
        return paymentProcessorService.updatePaymentProcessorMerchants(id, paymentProcessorMerchants);
    }

    @ApiOperation(value = "deletePaymentProcessor", nickname = "deletePaymentProcessor")
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 204, message = "Success"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> delete(@PathVariable Long id) {
    	
    	LOGGER.info(LoggingUtil.adminAuditInfo("Payment Processor Delete Request", BluefinWebPortalConstants.SEPARATOR,
    			"Payment Processor Id : ", String.valueOf(id)));
    	
        paymentProcessorService.deletePaymentProcessor(id);
        LOGGER.debug("Payment Processor {} has been deleted.", id);

        return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
    }
}
