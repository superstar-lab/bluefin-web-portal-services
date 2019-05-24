package com.mcmcg.ico.bluefin.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcmcg.ico.bluefin.batch.file.ACFBatchReturnFile;
import com.mcmcg.ico.bluefin.batch.file.MCMBatchReturnFile;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class BatchReturnFileObjectFactory {
	
	@Autowired
	MCMBatchReturnFile mCMBatchReturnFile;
	@Autowired
	ACFBatchReturnFile aCFBatchReturnFile;

	public BatchReturnFile getBatchFileObject(String legalEntityName) {
		
		if("MCM-LATITUDE".equalsIgnoreCase(legalEntityName)) {
			return mCMBatchReturnFile;
		}
		
		if("ACF-LATITUDE".equalsIgnoreCase(legalEntityName)) {
			return aCFBatchReturnFile;
		}
		
		throw new CustomException("Legal entity ["+legalEntityName+"] not available to generate batch return file");
	}
}
