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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.Transaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
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
		LOGGER.info("Getting transaction information by id = [{}], transactionType = [{}] and ", transactionId,
				transactionType, processorTransactionType);

		return paymentProcessorRemittanceService.getTransactionInformation(transactionId,
				TransactionTypeCode.valueOf(transactionType.toUpperCase()), processorTransactionType);
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
			@RequestParam(value = "sort", required = false) String sort) throws JsonProcessingException {
		LOGGER.info("Generating report with the following filters: {}", search);

		boolean negate = false;

		// For 'Not Reconciled' status, which is not in the database, simply
		// use: WHERE ReconciliationID != 'Reconciled'
		String reconciliationStatusId = paymentProcessorRemittanceService.getValueFromParameter(search,
				"reconciliationStatusId");
		if (reconciliationStatusId != null) {
			if (reconciliationStatusId.equals("notReconciled")) {
				String id = paymentProcessorRemittanceService.getReconciliationStatusId("Reconciled");
				search = search.replaceAll("notReconciled", id);
				negate = true;
			}
		}

		QueryDSLUtil.createExpression(search, SaleTransaction.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		String json = objectMapper.writerWithView(Views.Summary.class)
				.writeValueAsString(paymentProcessorRemittanceService.getRemittanceSaleRefundVoidTransactions(search,
						QueryDSLUtil.getPageRequest(page, size, sort), negate));

		return json;
	}
}