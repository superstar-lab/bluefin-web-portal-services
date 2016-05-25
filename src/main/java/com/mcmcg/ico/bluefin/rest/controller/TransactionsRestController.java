package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/transactions")
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);

    @ApiOperation(value = "getAuthorization", nickname = "getAuthorization")
    @RequestMapping(method = RequestMethod.GET, value = "/{transactionId}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Message not found", response = String.class),
            @ApiResponse(code = 500, message = "Failure") })
    public String getAuthorization(@PathVariable("transactionId") String transactionId) {
        LOGGER.info("Add implementation** get authorization");
        return "Add implementation** get authorization";
    }

}