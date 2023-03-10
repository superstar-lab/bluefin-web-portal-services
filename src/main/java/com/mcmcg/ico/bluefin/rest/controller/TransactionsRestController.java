package com.mcmcg.ico.bluefin.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.dto.PageDTO;
import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;
import com.mcmcg.ico.bluefin.model.*;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomBadRequestException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.Views;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.BatchUploadService;
import com.mcmcg.ico.bluefin.service.TransactionService;
import com.mcmcg.ico.bluefin.service.TransactionSummaryService;
import com.mcmcg.ico.bluefin.service.TransactionUpdateService;
import com.mcmcg.ico.bluefin.service.util.QueryUtil;
import com.mcmcg.ico.bluefin.util.DateTimeUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mcmcg.ico.bluefin.util.PageUtils.getPageRequest;

@RestController
@RequestMapping(value = "/api/transactions")
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);

    private static final  Map<String, Long> EMPTY_METRICS_RESPONSE =  new HashMap<String, Long>() {{
        put(BluefinWebPortalConstants.APPROVED_TRANSACTIONS_METRIC, 0L);
        put(BluefinWebPortalConstants.DECLINED_TRANSACTIONS_METRIC, 0L);
        put("Total Transactions", 0L);
    }};

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private TransactionSummaryService transactionSummaryService;
    @Autowired
    private TransactionUpdateService transactionUpdateService;
    @Autowired
    private BatchUploadService batchUploadService;

	@ApiOperation(value = "Generate Top Transaction Summary")
	@GetMapping(value = "/generateTopSummaryReport", produces = { "application/json" })
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = TopTranSummaryDTO.class, responseContainer = "Map"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<Map<String, List<TopTranSummaryDTO>>> generateTopSummaryReport(
			@RequestParam String top, @RequestParam String statusCode, @RequestParam String fromDate, @RequestParam String toDate, @RequestParam String timeZone) {
		LOGGER.info("Generate Top Summary Report From: {} To: {} TimeZone: {}", fromDate, toDate, timeZone);

		fromDate = DateTimeUtil.datetimeToUTC(fromDate.concat(" 00:00:00"), timeZone);
		toDate = DateTimeUtil.datetimeToUTC(toDate.concat(" 23:59:59"), timeZone);

        Map<String, List<TopTranSummaryDTO>> topList = transactionSummaryService.topSummary(top, statusCode, fromDate, toDate);
        if (topList.isEmpty()) {
            throw new CustomNotFoundException("Top Summary Report not found");
        }
        return new ResponseEntity<>(topList, HttpStatus.OK);
    }


	@ApiOperation(value = "Generate Declined Transaction Summary")
	@GetMapping(value = "/generateDeclinedReport", produces = { "application/json" })
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = DeclinedTranSummaryDTO.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<List<DeclinedTranSummaryDTO>> generateDeclinedReport(
			@RequestParam String fromDate, @RequestParam String toDate, @RequestParam String timeZone) {
		LOGGER.info("Generate Declined Report From: {} To: {} TimeZone: {}", fromDate, toDate, timeZone);

		fromDate = DateTimeUtil.datetimeToUTC(fromDate.concat(" 00:00:00"), timeZone);
		toDate = DateTimeUtil.datetimeToUTC(toDate.concat(" 23:59:59"), timeZone);

        List<DeclinedTranSummaryDTO> declineList = transactionSummaryService.declinedSummary(fromDate, toDate);
        if (declineList.isEmpty()) {
            throw new CustomNotFoundException("Decline Summary not found");
        }
        return new ResponseEntity<>(declineList, HttpStatus.OK);
    }

	@ApiOperation(value = "Generate Approved Transaction Summary")
	@GetMapping(value = "/generateApprovedReport", produces = { "application/json" })
	@ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ApprovedTranSummary.class, responseContainer = "Map"),
			@ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
			@ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
	public ResponseEntity<Map<String, List<ApprovedTranSummary>>> generateApprovedReport(
			@RequestParam String fromDate, @RequestParam String toDate, @RequestParam String timeZone) {

		fromDate = DateTimeUtil.datetimeToUTC(fromDate.concat(" 00:00:00"), timeZone);
		toDate = DateTimeUtil.datetimeToUTC(toDate.concat(" 23:59:59"), timeZone);

        LOGGER.info("Generate Approved Report From: {} To: {} TimeZone: {}", fromDate, toDate, timeZone);

        Map<String, List<ApprovedTranSummary>> approveList = transactionSummaryService.approvedSummary(fromDate, toDate);
        if (approveList.isEmpty()) {
            throw new CustomNotFoundException("Approved Summary not found");
        }
        return new ResponseEntity<>(approveList, HttpStatus.OK);
    }

    @ApiOperation(value = "getTransaction", nickname = "getTransaction")
    @GetMapping(value = "/{transactionId}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SaleTransaction.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public Transaction get(@PathVariable("transactionId") String transactionId,
                           @RequestParam(value = "type", required = false, defaultValue = "SALE") String type) {
        LOGGER.debug("Getting transaction information by id = [{}] and type = [{}]", transactionId, type);

        return transactionService.getTransactionInformation(transactionId,
                TransactionTypeCode.valueOf(type.toUpperCase()));
    }

    @ApiOperation(value = "getTransactions", nickname = "getTransactions")
    @GetMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public String get(@RequestParam(value = "search") String search,
                      @RequestParam(value = "page") Integer page,
                      @RequestParam(value = "size") Integer size,
                      @RequestParam(value = "sort", required = false) String sort,
                      @RequestParam(value = "timeZone") String timeZone,
                      @ApiIgnore Authentication authentication)
            throws JsonProcessingException {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.debug("get Transactions service");
        String searchValue;
        Map<String, List<String>> multipleValuesMap = new HashMap<>();

        if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
            searchValue = QueryUtil.getValidSearchBasedOnLegalEntities(userLE, search);
        } else {
            searchValue = search;
        }

        LOGGER.info("Generating Report with the following filters= {}", searchValue);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper.writerWithView(Views.Summary.class).writeValueAsString(
                transactionService.getTransactions(searchValue, multipleValuesMap,
                        QueryUtil.getPageRequest(page, size, sort), timeZone));
    }

    @ApiOperation(value = "getTransactions", nickname = "getTransactions")
    @PostMapping(produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header"),
            @ApiImplicitParam(name = "request", value = "request", required = true, paramType = "body")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public String post(@ApiIgnore MultipartHttpServletRequest request, @RequestParam(value = "search") String search,
                       @RequestParam(value = "page") Integer page,
                       @RequestParam(value = "size") Integer size,
                       @RequestParam(value = "sort", required = false) String sort,
                       @RequestParam(value = "timeZone") String timeZone,
                       @ApiIgnore Authentication authentication)
            throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        LOGGER.debug("get Transactions service");
        Map<String, MultipartFile> filesMap = request.getFileMap();
        MultipartFile[] filesArray = getFilesArray(filesMap);
        if (filesArray.length == 0 || filesArray.length > 2) {
            throw new CustomBadRequestException("A file must be uploaded");
        }

        Map<String, List<String>> multipleValuesMap = transactionService.getValuesFromFiles(filesArray);

        String searchValue;
        if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
            searchValue = QueryUtil.getValidSearchBasedOnLegalEntities(userLE, search);
        } else {
            searchValue = search;
        }

        LOGGER.info("Generating Report with the following filters= {}", searchValue);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper.writerWithView(Views.Summary.class).writeValueAsString(
                transactionService.getTransactions(searchValue, multipleValuesMap,
                        QueryUtil.getPageRequest(page, size, sort), timeZone));
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

    @ApiOperation(value = "Get Transactions from Updates")
    @PostMapping(value = "/update", produces = {"application/json"})
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public ResponseEntity<String> getTransactionsFromUpdates(
            @RequestParam String fromDate,
            @RequestParam String timeZone,
            @RequestBody List<UpdateInfo> updates, HttpServletResponse response) {
        LOGGER.info("Get Transactions from Updates, updates: {}", updates);
        try {
            return transactionUpdateService.getTransactionsFromUpdateReport(updates, fromDate, timeZone, response);
        } catch (Exception e) {
            LOGGER.error("TransactionsRestController -> getTransactionsFromUpdates. An error occurred during downloading file: {}", e.getMessage());
            throw new CustomException("An error occurred during downloading file.");
        }
    }

    @ApiOperation(value = "Get Transactions Metrics from Updates")
    @PostMapping(value = "/update/metrics", produces = {"application/json"})
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "Map"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public ResponseEntity<Map<String, Long>> getTransactionsMetricsFromUpdates(
            @RequestParam String fromDate,
            @RequestParam String timeZone,
            @RequestBody List<UpdateInfo> updates) {
        LOGGER.info("Get Transactions Metrics from Updates, updates: {}", updates);
        if(updates.isEmpty()) return new ResponseEntity<>(EMPTY_METRICS_RESPONSE, HttpStatus.OK);
        Map<String, Long> result = transactionUpdateService.getTransactionsFromUpdatesMetrics(updates, fromDate, timeZone);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @ApiOperation(value = "Get Transactions from Update")
    @GetMapping(value = "/update/{updateRequest}", produces = {"application/json"})
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "OK", response = SaleTransactionInfo.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public ResponseEntity<Page<SaleTransactionInfo>> getTransactionsFromUpdate(@PathVariable String updateRequest,
                                                                               @RequestParam String application, @RequestParam String updateDate, PageDTO pageDTO) {
        LOGGER.info("Get Transactions from Update, updateRequest: {}, application: {}, updateDate: {}, page: {}", updateRequest, application, updateDate, pageDTO);
        Page<SaleTransactionInfo> result = transactionUpdateService.getTransactionsFromUpdate(updateRequest, application, updateDate,
                getPageRequest(pageDTO.getPage(), pageDTO.getSize()));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}