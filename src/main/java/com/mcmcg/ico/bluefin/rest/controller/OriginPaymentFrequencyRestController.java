package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.OriginPaymentFrequency;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.OriginPaymentFrequencyService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/origin-payment-frequencies")
public class OriginPaymentFrequencyRestController {

	@Autowired
	private OriginPaymentFrequencyService originPaymentFrequencyService;
	private static final Logger LOGGER = LoggerFactory.getLogger(OriginPaymentFrequencyRestController.class);

	@ApiOperation(value = "getOriginPaymentFrequencies", nickname = "Get Origin Payment Frequencies")
	@GetMapping(produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = OriginPaymentFrequency.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public List<OriginPaymentFrequency> get(@ApiIgnore Authentication authentication) {
		// Added condition for an auth. user as it was not being used and sonar raise major issue for same.
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.info("Getting Origin Payment Frequency list.");
		return originPaymentFrequencyService.getOriginPaymentFrequencies();
	}
}
