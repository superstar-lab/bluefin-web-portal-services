package com.mcmcg.ico.bluefin.rest.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.BatchUploadService;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRemittanceService;
import com.mcmcg.ico.bluefin.service.TransactionService;
import com.mcmcg.ico.bluefin.service.util.ApplicationUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/reports")
public class ReportRestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReportRestController.class);
	private static final String DELETETEMPFILE = "Deleting temp file: {}";
	
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private BatchUploadService batchUploadService;
	@Autowired
	private PaymentProcessorRemittanceService paymentProcessorRemittanceService;

	@ApiOperation(value = "getTransactionsReport", nickname = "getTransactionsReport")
	@RequestMapping(method = RequestMethod.GET, value = "/transactions")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> getTransactionsReport(@RequestParam(value = "search", required = true) String search,
			@RequestParam(value = "timeZone", required = true) String timeZone,
			@ApiIgnore Authentication authentication, HttpServletResponse response) throws IOException {
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}

		LOGGER.debug("transactions service ::: Entered : search "+search);
		String searchValue;
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchValue = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchValue = search;
		}

		File downloadFile = transactionService.getTransactionsReport(searchValue, timeZone);
		InputStream targetStream = FileUtils.openInputStream(downloadFile);
		response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
		response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());
		
		// Below line found in releases while merging, but was not available in develop branch
		FileCopyUtils.copy(targetStream, response.getOutputStream());
		LOGGER.debug(DELETETEMPFILE, downloadFile.getName());
		downloadFile.delete();
		return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "getRemittanceTransactionsReport", nickname = "getRemittanceTransactionsReport")
	@RequestMapping(method = RequestMethod.GET, value = "/payment-processor-remittances")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = PaymentProcessorRemittance.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> getRemittanceTransactionsReport(
			@RequestParam(value = "search", required = true) String search,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "timeZone", required = true) String timeZone,
			@ApiIgnore Authentication authentication, HttpServletResponse response) throws IOException {
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		LOGGER.debug("payment-processor-remittances service ::: Entered : search "+search);
		String searchVal;
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchVal = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchVal = search;
		}

		boolean negate = false;
		// For 'Not Reconciled' status, which is not in the database, simply
		// use: WHERE ReconciliationID != 'Reconciled'
		String reconciliationStatusId = ApplicationUtil.getValueFromParameter(searchVal,"reconciliationStatusId");
		LOGGER.debug("payment-processor-remittances service ::: reconciliationStatusId : "+reconciliationStatusId);
		if ("notReconciled".equals(reconciliationStatusId)) {
			String id = paymentProcessorRemittanceService.getReconciliationStatusId("Reconciled");
			searchVal = searchVal.replaceAll("notReconciled", id);
			negate = true;
		}
		
		File downloadFile = transactionService.getRemittanceTransactionsReport(searchVal, timeZone,negate);
		InputStream targetStream = FileUtils.openInputStream(downloadFile);
		response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
		response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());

		FileCopyUtils.copy(targetStream, response.getOutputStream());
		LOGGER.debug(DELETETEMPFILE, downloadFile.getName());
		downloadFile.delete();
		return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "getBatchUploadsReport", nickname = "getBatchUploadsReport")
	@RequestMapping(method = RequestMethod.GET, value = "/batch-uploads", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = BatchUpload.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> get(@RequestParam(value = "noofdays", required = false) Integer noofdays,
			@RequestParam(value = "timeZone", required = true) String timeZone, HttpServletResponse response)
			throws IOException {
		LOGGER.info("Getting all batch uploads");
		File downloadFile = batchUploadService.getBatchUploadsReport(noofdays, timeZone);

		InputStream targetStream = FileUtils.openInputStream(downloadFile);
		response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
		response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());

		FileCopyUtils.copy(targetStream, response.getOutputStream());
		LOGGER.debug(DELETETEMPFILE, downloadFile.getName());
		downloadFile.delete();
		return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "getBatchUploadTransactionsReport", nickname = "getBatchUploadTransactionsReport")
	@RequestMapping(method = RequestMethod.GET, value = "/batch-upload-transactions", produces = "application/json")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = BatchUpload.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> getBatchUploadTransactionsReport(
			@RequestParam(value = "batchUploadId", required = true) Long batchUploadId,
			@RequestParam(value = "timeZone", required = true) String timeZone, HttpServletResponse response)
			throws IOException {
		LOGGER.debug("Getting all batch uploads by id = [{}]", batchUploadId);
		File downloadFile = batchUploadService.getBatchUploadTransactionsReport(batchUploadId, timeZone);

		InputStream targetStream = FileUtils.openInputStream(downloadFile);
		response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
		response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());

		FileCopyUtils.copy(targetStream, response.getOutputStream());
		LOGGER.debug(DELETETEMPFILE, downloadFile.getName());
		downloadFile.delete();
		return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	}
}