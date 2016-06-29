package com.mcmcg.ico.bluefin.rest.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.LegalEntityAppService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/legal-entities")
public class LegalEntityAppRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityAppRestController.class);

    @Autowired
    private LegalEntityAppService legalEntityAppService;

    @ApiOperation(value = "getLegalEntities", nickname = "getLegalEntities")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LegalEntityApp.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public List<LegalEntityApp> getLegalEntities(@ApiIgnore Principal principal) {
        LOGGER.info("Getting all legal entities");
        if (principal == null) {
            throw new AccessDeniedException("An authorization token is required to request this resource");
        }
        return legalEntityAppService.getLegalEntities(principal.getName());
    }
}
