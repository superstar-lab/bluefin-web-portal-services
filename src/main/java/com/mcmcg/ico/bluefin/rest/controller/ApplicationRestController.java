package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.Application;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.ApplicationService;

@RestController
@RequestMapping(value = "/api")
public class ApplicationRestController {

	@Autowired
	private ApplicationService applicationService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRestController.class);

	@GetMapping(value = "/ping", produces = "application/json")
	public ResponseEntity<String> ping(@RequestParam(value = "param", required = false) String param) {
		LOGGER.debug("Status of the application endpoint. Param = {}", param);

		return new ResponseEntity<>("{ \"status\" : \"UP\" }", HttpStatus.OK);
	}

	@ApiOperation(value = "getApplications", nickname = "Get applications")
	@GetMapping(value = "/applications", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = Application.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public List<Application> get() {
		LOGGER.info("Getting Applications list.");
		return applicationService.getApplications();
	}
}