package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mcmcg.ico.bluefin.persistent.BatchUpload;
import com.mcmcg.ico.bluefin.rest.resource.ErrorResource;
import com.mcmcg.ico.bluefin.service.BatchUploadService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping(value = "/api/batch-upload")
public class BatchUploadRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadRestController.class);

    @Autowired
    private BatchUploadService batchUploadService;

    @ApiOperation(value = "getBatchUpload", nickname = "getBatchUpload")
    @RequestMapping(method = RequestMethod.GET, value = "{id}", produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = BatchUpload.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 404, message = "Not found", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public BatchUpload get(@PathVariable Long id) {
        LOGGER.info("Getting batch upload by id");
        return batchUploadService.getBatchUploadById(id);
    }

    @ApiOperation(value = "getBatchUploads", nickname = "getBatchUploads")
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    @ApiImplicitParam(name = "X-Auth-Token", value = "Authorization token", dataType = "string", paramType = "header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = BatchUpload.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorResource.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResource.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorResource.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorResource.class) })
    public Iterable<BatchUpload> get(@RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "15") int size,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "noofdays", required = false) Integer noofdays) {
        LOGGER.info("Getting all batch uploads");
        if (noofdays == null) {
            return batchUploadService.getAllBatchUploads(page, size, sort);
        } else {
            return batchUploadService.getBatchUploadsFilteredByNoofdays(page, size, sort, noofdays);
        }
    }
}
