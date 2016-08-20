package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.TransactionType;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.TransactionTypeService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/transaction-types")
public class TransactionTypeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionTypeController.class);

    @Autowired
    private TransactionTypeService transactionTypeService;

    @ApiOperation(value = "getTransactionTypes", nickname = "getTransactionTypes")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = TransactionType.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<TransactionType> getTransactionTypes(@ApiIgnore Authentication authentication) {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.info("Getting transaction type list");
        return transactionTypeService.getTransactionTypes();
    }
}
