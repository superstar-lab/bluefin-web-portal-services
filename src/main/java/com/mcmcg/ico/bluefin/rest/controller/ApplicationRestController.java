package com.mcmcg.ico.bluefin.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class ApplicationRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRestController.class);

    @RequestMapping(method = RequestMethod.GET, value = "/ping", produces = "application/json")
    public ResponseEntity<String> ping(@RequestParam(value = "param", required = false) String param) {
        LOGGER.debug("Status of the application endpoint. Param = [{}]", param);

        return new ResponseEntity<String>("{ \"status\" : \"UP\" }", HttpStatus.OK);
    }
}