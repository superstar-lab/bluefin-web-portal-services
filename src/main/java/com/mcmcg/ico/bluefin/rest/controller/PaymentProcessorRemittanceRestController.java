package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
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
    
    @ApiOperation(value = "getTransaction", nickname = "getTransaction")
    @RequestMapping(method = RequestMethod.GET, value = "/{transactionId}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Transaction get(@PathVariable("transactionId") String transactionId,
            @RequestParam(value = "transactionType", required = false, defaultValue = "SALE") String transactionType,
            @RequestParam(value = "processorTransactionType", required = true, defaultValue = "BlueFin") String processorTransactionType) {
        LOGGER.info("Getting transaction information by id = [{}], transactionType = [{}] and ", transactionId, transactionType, processorTransactionType);

        return paymentProcessorRemittanceService.getTransactionInformation(transactionId, TransactionType.valueOf(transactionType.toUpperCase()), processorTransactionType);
    }
    
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
        
        boolean negate = false;
        
        // For 'Not Reconciled' status, which is not in the database, simply use: WHERE ReconciliationID != 'Reconciled'
        String reconciliationStatusId = paymentProcessorRemittanceService.getValueFromParameter(search, "reconciliationStatusId");
        if (reconciliationStatusId != null) {
        	if (reconciliationStatusId.equals("notReconciled")) {
        		String id = paymentProcessorRemittanceService.getReconciliationStatusId("Reconciled");
        		search = search.replaceAll("notReconciled", id);
        		negate = true;
        	}
        }
        
        Iterable<PaymentProcessorRemittance> paymentProcessorRemittanceList = getPaymentProcessorRemittanceList(search, page, size, sort, negate);
        Iterable<SaleTransaction> saleTransactionList = getSaleTransactionList(search, page, size, sort, negate);
        
        String json = paymentProcessorRemittanceService.getAdjustedTransactions(paymentProcessorRemittanceList, saleTransactionList, QueryDSLUtil.getPageRequest(page, size, sort));
        
     	return json;
    }
    
    private Iterable<PaymentProcessorRemittance> getPaymentProcessorRemittanceList(String search, Integer page, Integer size, String sort, boolean negate) {
    	QueryDSLUtil.createExpression(search, PaymentProcessorRemittance.class);
 		return paymentProcessorRemittanceService.getPaymentProcessorRemittances(search, QueryDSLUtil.getPageRequest(page, size, sort), negate);
    }
    
    private Iterable<SaleTransaction> getSaleTransactionList(String search, Integer page, Integer size, String sort, boolean negate) {
    	
    	// SaleTransaction uses processorName not paymentProcessorId.
    	String id = paymentProcessorRemittanceService.getValueFromParameter(search, "paymentProcessorId");
    	if (id != null) {
    		String processorName = paymentProcessorRemittanceService.getProcessorNameById(id);
        	search = search.replaceAll("paymentProcessorId:" + id, "processorName:" + processorName);
    	}
    	
    	QueryDSLUtil.createExpression(search, SaleTransaction.class);
 		return paymentProcessorRemittanceService.getSalesRefundTransactions(search, QueryDSLUtil.getPageRequest(page, size, sort), negate);
    }
}
