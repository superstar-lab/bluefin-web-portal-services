package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.PaymentProcessorRuleTrends;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRuleService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/payment-processor-rule-trends")
public class PaymentProcessorRuleReportController {

	@Autowired
	PaymentProcessorRuleService paymentProcessorRuleService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentProcessorRuleReportController.class);

	@ApiOperation(value = "Get payment processor rules trends", nickname = "getPaymentProcessorRulesTrends")
	@GetMapping(produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PaymentProcessorRuleTrends.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public PaymentProcessorRuleTrends get(@RequestParam(value = "frequency") String frequency,
			@RequestParam(value = "startDate", required = true) String startDate,
			@RequestParam(value = "endDate", required = true) String endDate) {
		LOGGER.info("Getting Trends for Payment Processor");
		return paymentProcessorRuleService.getProcessorRuleTrendsListByFrequency(startDate, endDate, frequency);
	}

}
