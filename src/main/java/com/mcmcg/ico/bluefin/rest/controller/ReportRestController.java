package com.mcmcg.ico.bluefin.rest.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.factory.BatchReturnFileObjectFactory;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.BatchUploadService;
import com.mcmcg.ico.bluefin.service.PaymentProcessorRemittanceService;
import com.mcmcg.ico.bluefin.service.TransactionService;
import com.mcmcg.ico.bluefin.service.util.ApplicationUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
	@Autowired
	BatchReturnFileObjectFactory batchReturnFileObjectFactory;

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
		
		LOGGER.debug("search ={} ",search);
		String searchValue;
		List<String> accountList= new ArrayList<>();
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchValue = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchValue = search;
		}
		try {
			File downloadFile = transactionService.getTransactionsReport(searchValue, accountList, timeZone);
			
			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during downloading file="+e);
			throw new CustomException("An error occured to during downloading file.");
		}
	}
	
	@ApiOperation(value = "getTransactionsReport", nickname = "getTransactionsReport")
	@RequestMapping(method = RequestMethod.POST, value = "/transactions")
	@ApiImplicitParams({
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header"),
	@ApiImplicitParam(name = "request", value = "request", required = true, paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> getTransactionsReport(@ApiIgnore MultipartHttpServletRequest request,
			@RequestParam(value = "search", required = true) String search,
			@RequestParam(value = "timeZone", required = true) String timeZone,
			@ApiIgnore Authentication authentication, HttpServletResponse response) throws IOException {
		if (authentication == null) {
			throw new AccessDeniedException("An authorization token is required to request this resource");
		}
		
		LOGGER.debug("search ={} ",search);
		Map<String, MultipartFile> filesMap = request.getFileMap();
        MultipartFile[] filesArray = getFilesArray(filesMap);
        if (filesArray.length != 1) {
			throw new CustomBadRequestException("A file must be uploded");
		}
		List<String> accountList= transactionService.getAccountListFromFile(filesArray);
		if(accountList.size()==0){
	    	LOGGER.error("There is no record exist for this file");
			throw new CustomException("There is no record exist for this file.");
	    }
		String searchValue;
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchValue = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchValue = search;
		}
		try {
			File downloadFile = transactionService.getTransactionsReport(searchValue, accountList, timeZone);
			
			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during downloading file="+e);
			throw new CustomException("An error occured to during downloading file.");
		}
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
		LOGGER.debug("search {} , sort {} ",search,sort);
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
		LOGGER.debug("reconciliationStatusId : {}",reconciliationStatusId);
		if ("notReconciled".equals(reconciliationStatusId)) {
			String id = paymentProcessorRemittanceService.getReconciliationStatusId("Reconciled");
			searchVal = searchVal.replaceAll("notReconciled", id);
			negate = true;
		}
		try {
			File downloadFile = transactionService.getRemittanceTransactionsReport(searchVal, timeZone,negate);
			
			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during getRemittanceTransactionsReport file= "+e);
			throw new CustomException("An error occured to during getRemittanceTransactionsReport file.");
		}
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
		try {
			File downloadFile = batchUploadService.getBatchUploadsReport(noofdays, timeZone);

			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during get report="+e);
			throw new CustomException("An error occured to during get report.");
		}
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
		try {
			String legalEntityName = "";
			
			BatchReturnFileModel batchReturnFileModel = batchUploadService.getBatchUploadTransactionsReport(batchUploadId);
			if(batchReturnFileModel!=null && batchReturnFileModel.getBatchUpload()!=null) {
				legalEntityName = batchReturnFileModel.getBatchUpload().getLegalEntityName();
			}
			LOGGER.info("Legal Entity Name for batch return = [{}]", legalEntityName);
			BatchReturnFile batchReturnFile = batchReturnFileObjectFactory.getBatchFileObject(legalEntityName);
			Map<String, Object[]> fileHeadersMap = batchReturnFile.createFileHeader();
			Map<String, BatchFileObjects> batchFileObjectsMap = batchReturnFile.createFileMap(batchReturnFile,fileHeadersMap,legalEntityName, batchUploadId);
			Map<String, File> downloadFileMap = batchReturnFile.generateFile(batchReturnFile, batchReturnFileModel, batchFileObjectsMap, timeZone);

			return batchReturnFile.deleteTempFile(downloadFileMap, response, DELETETEMPFILE, batchUploadId, legalEntityName);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during get report="+e);
			throw new CustomException("An error occured for batch return file." + e.getMessage());
		}
	}
	
	 private MultipartFile[] getFilesArray(Map<String, MultipartFile> filesMap) {
	        Set<String> keysSet = filesMap.keySet();
	        MultipartFile[] fileArray = new MultipartFile[keysSet.size()];
	        int i = 0;
	        for (String key : keysSet) {
	            if (key.indexOf("file") != -1) {
	                fileArray[i] = filesMap.get(key);
	                i++;
	            }
	        }
	        return fileArray;

	    }
}