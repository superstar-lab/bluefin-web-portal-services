package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.rest.resource.Views;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.TransactionService;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/transactions")
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SessionService sessionService;

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
            @RequestParam(value = "type", required = false, defaultValue = "SALE") String type) {
        LOGGER.info("Getting transaction information by id = [{}] and type = [{}]", transactionId, type);

        return transactionService.getTransactionInformation(transactionId, TransactionType.valueOf(type.toUpperCase()));
    }

    @ApiOperation(value = "getTransactions", nickname = "getTransactions")
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
            @RequestParam(value = "sort", required = false) String sort, @ApiIgnore Authentication authentication)
            throws JsonProcessingException {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
            search = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
        }

        LOGGER.info("Generating report with the following filters: {}", search);

        QueryDSLUtil.createExpression(search, SaleTransaction.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return objectMapper.writerWithView(Views.Public.class).writeValueAsString(
                transactionService.getTransactions(search, QueryDSLUtil.getPageRequest(page, size, sort)));
    }
}