package com.mcmcg.ico.bluefin.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcmcg.ico.bluefin.model.ErrorObj;
import com.mcmcg.ico.bluefin.model.Merchant;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.service.MerchantService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api")
public class MerchantRestController {

    @Autowired
    public MerchantService merchantService;

    @ApiOperation(value = "Get All Merchants by Payment Processor")
    @GetMapping(value = "/merchant", produces = { "application/json" })
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Merchant.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorObj.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorObj.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorObj.class) })
    public ResponseEntity<List<Merchant>> getMerchants(Long paymentProcessorID) {
        log.info("Get All Merchants by Payment Processor ID: {}",  paymentProcessorID);
        List<Merchant> merchantList = merchantService.findByProcessorId(paymentProcessorID);
        return new ResponseEntity<>(merchantList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get All Merchants Ext Credit-Debit")
    @GetMapping(value = "/merchantExtCreditDebit", produces = { "application/json" })
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Merchant.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorObj.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorObj.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorObj.class) })
    public ResponseEntity<List<String>> getMerchantsExtCreditDebit(
            @RequestParam(value = "search", required = false) String search) throws JsonProcessingException {
        log.info("Get All Merchants EXT Credit-Debit" + search!=null? "search-> "+search:"");
        List<String> merchantList = merchantService.findAllMerchantsExtCreditDebit(search);
        return new ResponseEntity<>(merchantList, HttpStatus.OK);
    }

    @ApiOperation(value = "Put All Merchants by Payment Processor")
    @PutMapping(value = "/merchant", produces = { "application/json" })
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Merchant.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorObj.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorObj.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorObj.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorObj.class) })
    public ResponseEntity<String> saveMerchants(@RequestBody List<Merchant> merchants) {
        //log.info("Save Merchant {}",  merchant);
        boolean infoSaved = merchantService.save(merchants);
        if(!infoSaved) {
           // log.error("MerchantRestController -> saveMerchant, Error updating Merchant {}", merchant);
            throw new CustomException("Merchant update error");
        }
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }


}
