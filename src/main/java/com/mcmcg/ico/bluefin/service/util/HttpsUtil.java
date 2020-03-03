package com.mcmcg.ico.bluefin.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mcmcg.ico.bluefin.model.BatchFileRequest;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

public class HttpsUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsUtil.class);
	private HttpsUtil(){
		// default constructor
	}

    public static BatchUpload sendPostRequest(String requestUrl, Long batchId , String payload, String xAuthToken, String legalEntityName) {
    	
    	RestTemplate restTemplate = new RestTemplate();
	    BatchFileRequest request = new BatchFileRequest(batchId, payload, legalEntityName);
	     
	    HttpHeaders headers = new HttpHeaders();
	    headers.set("X-Auth-Token", xAuthToken); 
	    headers.add("Content-Type", "application/json");
	 
	    HttpEntity<BatchFileRequest> requestFinal = new HttpEntity<>(request, headers);
	    
	    ResponseEntity<BatchUpload> response = restTemplate.exchange(requestUrl, HttpMethod.POST, requestFinal, BatchUpload.class);
	    
	    if(response != null && response.getBody()!=null) {
	    	return response.getBody();
	    }
	    
	    LOGGER.error("Unable to get service response..");
	    throw new CustomException("Unable to get service response.");
	    
    }
}
