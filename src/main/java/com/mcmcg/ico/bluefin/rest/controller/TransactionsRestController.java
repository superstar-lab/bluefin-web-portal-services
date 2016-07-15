package com.mcmcg.ico.bluefin.rest.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.TransactionsService;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;
import com.mysema.query.types.expr.BooleanExpression;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/transactions")
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);
    private static final String TRANSACTION_ID_FILTER = "transactionId:";

    @Autowired
    private TransactionsService transactionService;

    @ApiOperation(value = "getTransaction", nickname = "getTransaction")
    @RequestMapping(method = RequestMethod.GET, value = "/{transactionId}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public SaleTransaction getTransaction(@PathVariable("transactionId") String transactionId) {
        LOGGER.info("Getting transaction information by id: {}", transactionId);
        return transactionService.getTransactionInformation(transactionId);
    }

    @ApiOperation(value = "getTransactions", nickname = "getTransactions")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<SaleTransaction> getTransactions(@RequestParam("search") String search,
            @RequestParam(value = "page") Integer page, @RequestParam(value = "size") Integer size,
            @RequestParam(value = "sort", required = false) String sort, @ApiIgnore Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        String username = principal.getName();
        List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(username);
        search = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);

        BooleanExpression transactionIdFilter = null;
        if (search.contains(TRANSACTION_ID_FILTER)) {
            String value = QueryDSLUtil.getTransactionIdValue(search, TRANSACTION_ID_FILTER);
            transactionIdFilter = QueryDSLUtil.getTransactionIdFilter(search, value);
            search = search.replace(TRANSACTION_ID_FILTER + value, "");
        }

        LOGGER.info("Generating report with the following filters: {}", search);
        BooleanExpression predicate = QueryDSLUtil.createExpression(search, SaleTransaction.class)
                .and(transactionIdFilter);

        return transactionService.getTransactions(predicate, page, size, sort);
    }
}