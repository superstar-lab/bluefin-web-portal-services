package com.mcmcg.ico.bluefin.rest.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.Views;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRemittanceService;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

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
    
    @ApiOperation(value = "getPaymentProcessorRemittances", nickname = "getPaymentProcessorRemittances")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public String get(@RequestParam(value = "search", required = true) String search,
            @RequestParam(value = "page", required = true) Integer page,
            @RequestParam(value = "size", required = true) Integer size,
            @RequestParam(value = "sort", required = false) String sort)
            throws JsonProcessingException {
        LOGGER.info("Generating report with the following filters: {}", search);
        
        String json = "";
        
        // Get reconciliation status map
        HashMap<String, String> reconciliationStatusMap = paymentProcessorRemittanceService.getReconciliationStatusMap();
        String salesKey = paymentProcessorRemittanceService.getKeyFromValue(reconciliationStatusMap, "Remit without Sale");
        String refundKey = paymentProcessorRemittanceService.getKeyFromValue(reconciliationStatusMap, "Remit without Refund");
        
        // Get reconciliation status ID
        String reconciliationStatus = paymentProcessorRemittanceService.getValueFromParameter(search, "reconciliationStatusId");
     	
     	if ((reconciliationStatus.equals(salesKey)) || (reconciliationStatus.equals(refundKey))) {
     		QueryDSLUtil.createExpression(search, PaymentProcessorRemittance.class);
     		ObjectMapper objectMapper = new ObjectMapper();
     		objectMapper.registerModule(new JodaModule());
     		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
     		json = objectMapper.writerWithView(Views.Summary.class).writeValueAsString(paymentProcessorRemittanceService.getPaymentProcessorRemittances(search, QueryDSLUtil.getPageRequest(page, size, sort)));
     	} else {
     		QueryDSLUtil.createExpression(search, SaleTransaction.class);
     		ObjectMapper objectMapper = new ObjectMapper();
     		objectMapper.registerModule(new JodaModule());
     		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
     		json = objectMapper.writerWithView(Views.Summary.class).writeValueAsString(paymentProcessorRemittanceService.getSalesRefundTransactions(search, QueryDSLUtil.getPageRequest(page, size, sort)));
     	}
        
     	return json;
    }
}
