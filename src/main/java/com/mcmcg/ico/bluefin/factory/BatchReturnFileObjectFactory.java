package com.mcmcg.ico.bluefin.factory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.batch.file.ACFBatchReturnFile;
import com.mcmcg.ico.bluefin.batch.file.MCMBatchReturnFile;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.service.PropertyService;

@Component
public class BatchReturnFileObjectFactory {
	
	@Value("${spring.bluefin.mcm.legal.entity}")
	private String mcmLatitude;
	
	@Value("${spring.bluefin.acf.legal.entity}")
	private String acfLatitude;
	
	@Value("${spring.bluefin.jpf.legal.entity}")
	private String jpfLatitude;
	
	@Autowired
	private PropertyService propertyService;
	@Autowired
	MCMBatchReturnFile mCMBatchReturnFile;
	@Autowired
	ACFBatchReturnFile aCFBatchReturnFile;

	public BatchReturnFile getBatchFileObject(String legalEntityName) {
		String mcmLatitudeConfig = StringUtils.isNotBlank(propertyService.getPropertyValue(BluefinWebPortalConstants.MCMLEGALENTITYNAMECONFIG)) ?
				propertyService.getPropertyValue(BluefinWebPortalConstants.MCMLEGALENTITYNAMECONFIG) : mcmLatitude;
		String acfLatitudeConfig = StringUtils.isNotBlank(propertyService.getPropertyValue(BluefinWebPortalConstants.ACFLEGALENTITYNAMECONFIG)) ?
				propertyService.getPropertyValue(BluefinWebPortalConstants.ACFLEGALENTITYNAMECONFIG) : acfLatitude;
		String jpfLatitudeConfig = StringUtils.isNotBlank(propertyService.getPropertyValue(BluefinWebPortalConstants.JPFLEGALENTITYNAMECONFIG)) ?
				propertyService.getPropertyValue(BluefinWebPortalConstants.JPFLEGALENTITYNAMECONFIG) : jpfLatitude;

		if(mcmLatitudeConfig.equalsIgnoreCase(legalEntityName)) {
			return mCMBatchReturnFile;
		}
			
		if(acfLatitudeConfig.equalsIgnoreCase(legalEntityName) || jpfLatitudeConfig.equalsIgnoreCase(legalEntityName)) {
			return aCFBatchReturnFile;
		}
		
		throw new CustomException("Legal entity ["+legalEntityName+"] not available to generate batch return file");
	}
}
