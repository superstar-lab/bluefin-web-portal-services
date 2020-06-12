package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.ReconciliationStatusService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/reconciliation-status")
public class ReconciliationStatusRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReconciliationStatusRestController.class);

	@Autowired
	private ReconciliationStatusService reconciliationStatusService;

	@ApiOperation(value = "getReconciliationStatus", nickname = "getReconciliationStatus")
	@GetMapping(value = "{id}", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ReconciliationStatus.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ReconciliationStatus get(@PathVariable Long id) {
		LOGGER.debug("Getting reconciliation status with id ={}", id);
		return reconciliationStatusService.getReconciliationStatusById(id);
	}

	@ApiOperation(value = "getReconciliationStatuses", nickname = "getReconciliationStatuses")
	@GetMapping(produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = ReconciliationStatus.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public List<ReconciliationStatus> get() {
		LOGGER.info("Getting all reconciliation statuses");
		return reconciliationStatusService.getReconciliationStatuses();
	}
}
