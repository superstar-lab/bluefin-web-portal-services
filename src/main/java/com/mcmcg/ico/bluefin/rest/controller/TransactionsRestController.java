package com.mcmcg.ico.bluefin.rest.controller;

import org.omg.IOP.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.service.TransactionsService;
import com.mcmcg.paymentprocessor.ACH1;
import com.mcmcg.paymentprocessor.CreditCard;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsRestController.class);

    @Autowired
    TransactionsService transactionsService;

    @ApiOperation(value = "payment", nickname = "payment")
    @RequestMapping(method = RequestMethod.POST, value = "/{id}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Message not found", response = String.class),
            @ApiResponse(code = 500, message = "Failure") })
    public String payment(@PathVariable("id") String id) {
        LOGGER.info("Calling getTransaction with the following id: {}", id);
        // TODO: add parameters to credit card and ach1
        CreditCard creditCard = new CreditCard();
        ACH1 ach1 = new ACH1();
        transactionsService.payment(creditCard, ach1);
        return id;
    }

}