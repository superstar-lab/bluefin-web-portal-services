package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRemittanceService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/payment-processor-remittances")
public class PaymentProcessorRemittanceRestController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRemittanceRestController.class);

    @Autowired
    private PaymentProcessorRemittanceService paymentProcessorRemittanceService;
    
    @ApiOperation(value = "getPaymentProcessorRemittance", nickname = "getPaymentProcessorRemittance")
    @RequestMapping(method = RequestMethod.GET, value = "{id}", produces = "application/json")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRemittance.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public PaymentProcessorRemittance get(@PathVariable Long id) {
        LOGGER.info(String.format("Getting payment processor remittance with id = [%s]", id));
        return paymentProcessorRemittanceService.getPaymentProcessorRemittanceById(id);
    }
    
    @ApiOperation(value = "getPaymentProcessorRemittances", nickname = "getPaymentProcessorRemittances")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentProcessorRemittance.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<PaymentProcessorRemittance> get() {
        LOGGER.info("Getting all payment processor remittances");
        return paymentProcessorRemittanceService.getPaymentProcessorRemittances();
    }
}
