package com.mcmcg.ico.bluefin.rest.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/rest/bluefin/legal-entities")
public class LegalEntityAppRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppRestController.class);

    @Autowired
    private LegalEntityAppService legalEntityAppService;

    @ApiOperation(value = "getLegalEntities", nickname = "getLegalEntities")
    @RequestMapping(method = RequestMethod.GET)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", response = String.class),
            @ApiResponse(code = 404, message = "Message not found", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 500, message = "Failure") })
    public List<LegalEntityApp> getLegalEntities() throws Exception {
        LOGGER.info("get All Legal Entities");
        return legalEntityAppService.findAll();
    }
}
