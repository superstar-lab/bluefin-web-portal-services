package com.mcmcg.ico.bluefin.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.model.AccountValidation;
import com.mcmcg.ico.bluefin.model.AccountValidationRequest;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.AccountValidationService;
import com.mcmcg.ico.bluefin.service.util.QueryUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/account-validation")
public class AccountValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountValidationController.class);

    @Autowired
    private AccountValidationService accountValidationService;

    @ApiOperation(value = "getAccountValidation", nickname = "getAccountValidations")
    @GetMapping(produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AccountValidation.class, responseContainer = "Map"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public Iterable<AccountValidation> get(@ApiIgnore Authentication authentication)
            throws JsonProcessingException {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        return accountValidationService.getAccountValidations();
    }


    // Returning account validation list by Post method and paging
    @ApiOperation(value = "getAccountValidation", nickname = "getAccountValidations")
    @PostMapping(produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class)})
    public Map<String, Object> post(
            @RequestBody AccountValidationRequest accountValidationRequestFilter,
            @ApiIgnore Authentication authentication) throws IOException {

        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        LOGGER.debug("get Account Validation service");
        LOGGER.info(" startDate = {}, page = {} ", accountValidationRequestFilter.getStartDate(), accountValidationRequestFilter.getPage());

        Integer page = accountValidationRequestFilter.getPage();
        Integer size = accountValidationRequestFilter.getSize();
        String sort = accountValidationRequestFilter.getSort();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JodaModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return accountValidationService.getAccountValidationFilter(accountValidationRequestFilter.getStartDate(),
                accountValidationRequestFilter.getEndDate(), QueryUtil.getPageRequest(page, size, sort));
    }

    private static Date getParseDate(String date) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setLenient(false);
            return df.parse(date);
        } catch (ParseException e) {
            LOGGER.error("Unable to parse date value");
            return null;
        }
    }

}
