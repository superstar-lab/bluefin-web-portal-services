package com.mcmcg.ico.bluefin.rest.controller;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.factory.BatchReturnFileObjectFactory;
import com.mcmcg.ico.bluefin.model.*;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.*;
import com.mcmcg.ico.bluefin.service.util.ApplicationUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	@Autowired
	private UserService userService;
	@Autowired
	private TransactionSummaryService transactionSummaryService;
	
	private static final String ACCESS_DENIED_ERROR = "An authorization token is required to request this resource";

	@ApiOperation(value = "getTransactionsReport", nickname = "getTransactionsReport")
	@GetMapping(value = "/transactions")
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
			throw new AccessDeniedException(ACCESS_DENIED_ERROR);
		}
		
		LOGGER.debug("search ={} ",search);
		String searchValue;
		Map<String, List<String>> multipleValuesMap = new HashMap<>();
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchValue = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchValue = search;
		}
		try {
			File downloadFile = transactionService.getTransactionsReport(searchValue, multipleValuesMap, timeZone);
			
			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during downloading file="+e);
			throw new CustomException("An error occured to during downloading file.");
		}
	}
	
	@ApiOperation(value = "getTransactionsReport", nickname = "getTransactionsReport")
	@PostMapping(value = "/transactions")
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
			throw new AccessDeniedException(ACCESS_DENIED_ERROR);
		}
		
		LOGGER.debug("search ={} ",search);
		Map<String, MultipartFile> filesMap = request.getFileMap();
        MultipartFile[] filesArray = getFilesArray(filesMap);
		if (filesArray.length == 0 || filesArray.length > 2) {
			throw new CustomBadRequestException("A file must be uploaded");
		}

		Map<String, List<String>> multipleValuesMap = transactionService.getValuesFromFiles(filesArray);

		String searchValue;
		if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
			List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
			searchValue = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
		} else {
			searchValue = search;
		}
		try {
			File downloadFile = transactionService.getTransactionsReport(searchValue, multipleValuesMap, timeZone);
			
			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);
			
		}
		catch(Exception e) {
			LOGGER.error("An error occured to during downloading file="+e);
			throw new CustomException("An error occured to during downloading file.");
		}
	}

	@ApiOperation(value = "getRemittanceTransactionsReport", nickname = "getRemittanceTransactionsReport")
	@GetMapping(value = "/payment-processor-remittances")
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
			throw new AccessDeniedException(ACCESS_DENIED_ERROR);
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
			searchVal = searchVal.replace("notReconciled", id);
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
	@GetMapping(value = "/batch-uploads", produces = "application/json")
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
	@GetMapping(value = "/batch-upload-transactions", produces = "application/json")
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

	@ApiOperation(value = "getUsersReport", nickname = "getUsersReport")
	@GetMapping(value = "/user")
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<String> getUsersReport(@RequestParam(value = "search", required = true) String search,
												 @ApiIgnore Authentication authentication, HttpServletResponse response) throws IOException {
		if (authentication == null) {
			throw new AccessDeniedException(BluefinWebPortalConstants.AUTHTOKENREQUIRERESOURCEMSG);
		}

		LOGGER.info("get User Report");
		final String userName = authentication.getName();
		LOGGER.debug("userName ={} ",userName);

		int anyOtherParamsIndex = search.indexOf('&');
		if (anyOtherParamsIndex != -1 && anyOtherParamsIndex < search.length()) {
			search = search.substring(0, anyOtherParamsIndex);
		}

		try {
			File downloadFile = userService.getUsersReport(search);

 			return batchUploadService.deleteTempFile(downloadFile, response, DELETETEMPFILE);

		} catch(Exception e) {
			LOGGER.error("An error occurred during downloading file="+e);
			throw new CustomException("An error occurred during downloading file.");
		}
	}


	@ApiOperation(value = "Generate Transaction Summary Report")
	@GetMapping(value = "/transactions/summary-report", produces = {"application/json"})
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
	public ResponseEntity<String> generateTransactionSummaryReport(@RequestParam String top, @RequestParam String statusCode,
																   @RequestParam String fromDate, @RequestParam String toDate, HttpServletResponse response) {
		LOGGER.info("Generate Transaction Summary Excel Report Top: {} Status Code: {} From: {} To: {}", top, statusCode, fromDate, toDate);

		try {
			File summaryReportList = transactionSummaryService.tranSummaryReport(top, statusCode, fromDate, toDate);

			return batchUploadService.deleteTempFile(summaryReportList, response, DELETETEMPFILE);


		} catch(Exception e) {
			LOGGER.error("An error occurred during downloading transaction summary Top: {} Status Code: {} From: {} To: {} Error: {}",
					top, statusCode, fromDate, toDate, e.toString());
			throw new CustomException("An error occurred during downloading transaction summary file.");
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