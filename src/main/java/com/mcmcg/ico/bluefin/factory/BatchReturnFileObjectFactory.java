package com.mcmcg.ico.bluefin.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.batch.file.ACFBatchReturnFile;
import com.mcmcg.ico.bluefin.batch.file.MCMBatchReturnFile;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class BatchReturnFileObjectFactory {
	
	@Value("${spring.bluefin.mcm.legal.entity}")
	private String mcmLatitude;
	
	@Value("${spring.bluefin.acf.legal.entity}")
	private String acfLatitude;
	
	@Value("${spring.bluefin.jpf.legal.entity}")
	private String jpfLatitude;
	
	
	@Autowired
	MCMBatchReturnFile mCMBatchReturnFile;
	@Autowired
	ACFBatchReturnFile aCFBatchReturnFile;

	public BatchReturnFile getBatchFileObject(String legalEntityName) {
		String legalEntityPrifix = legalEntityName.substring(0, legalEntityName.indexOf('-'));
		if(mcmLatitude.equalsIgnoreCase(legalEntityPrifix)) {
			return mCMBatchReturnFile;
		}
		
		if(acfLatitude.equalsIgnoreCase(legalEntityPrifix) || jpfLatitude.equalsIgnoreCase(legalEntityPrifix)) {
			return aCFBatchReturnFile;
		}
		
		throw new CustomException("Legal entity ["+legalEntityName+"] not available to generate batch return file");
	}
}
