package com.mcmcg.ico.bluefin.rest.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.security.service.SessionService;
import com.mcmcg.ico.bluefin.service.TransactionsService;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/reports")
public class ReportRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRestController.class);

    @Autowired
    private TransactionsService transactionService;
    @Autowired
    private SessionService sessionService;

    @ApiOperation(value = "getTransactionsReport", nickname = "getTransactionsReport")
    @RequestMapping(method = RequestMethod.GET, value = "/transactions")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SaleTransaction.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public ResponseEntity<String> getTransactionsReport(@RequestParam(value = "search", required = true) String search,
            @RequestParam(value = "sort", required = false) String sort, @ApiIgnore Authentication authentication,
            HttpServletResponse response) throws IOException {
        if (authentication == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }

        if (!sessionService.sessionHasPermissionToManageAllLegalEntities(authentication)) {
            List<LegalEntityApp> userLE = transactionService.getLegalEntitiesFromUser(authentication.getName());
            search = QueryDSLUtil.getValidSearchBasedOnLegalEntities(userLE, search);
        }

        File downloadFile = transactionService.getTransactionsReport(search);
        InputStream targetStream = FileUtils.openInputStream(downloadFile);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + downloadFile.getName());

        FileCopyUtils.copy(targetStream, response.getOutputStream());
        LOGGER.info("Deleting temp file: {}", downloadFile.getName());
        downloadFile.delete();
        return new ResponseEntity<String>("{}", HttpStatus.NO_CONTENT);
    }
}