package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.TransactionView;
import com.mcmcg.ico.bluefin.service.TransactionsService;
import com.mcmcg.ico.bluefin.service.util.QueryDSLUtil;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/transactions")
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);

    @Autowired
    private TransactionsService transactionService;

    @ApiOperation(value = "getTransaction", nickname = "getTransaction")
    @RequestMapping(method = RequestMethod.GET, value = "/{transactionId}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Transaction not found", response = String.class),
            @ApiResponse(code = 500, message = "Failure") })
    public TransactionView getTransaction(@PathVariable("transactionId") String transactionId) {
        LOGGER.info("Getting transaction information by id: {}", transactionId);
        return transactionService.getTransactionInformation(transactionId);

    }

    @ApiOperation(value = "getTransactions", nickname = "getTransactions")
    @RequestMapping(method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Transactions not found", response = String.class),
            @ApiResponse(code = 500, message = "Failure") })
    public Iterable<TransactionView> getTransactions(@RequestParam("search") String search,
            @RequestParam(value = "page") Integer page, @RequestParam(value = "size") Integer size,
            @RequestParam(value = "sort", required = false) String sort) {
        LOGGER.info("Generating report with the following filters: {}", search);
        return transactionService.getTransactions(QueryDSLUtil.createExpression(search), page, size, sort);

    }

}