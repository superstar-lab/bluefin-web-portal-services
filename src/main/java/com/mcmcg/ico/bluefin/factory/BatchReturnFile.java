package com.mcmcg.ico.bluefin.factory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.service.PropertyService;

public abstract class BatchReturnFile {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchReturnFile.class);
	
	@Autowired
	private PropertyService propertyService; 
	
	public abstract Map<String, Object[]> createFileHeader();
	
	public abstract Map<String, BatchFileObjects> createFile(Map<String, Object[]> fileHeadersMap, String legalEntityName, String reportPath, Map<String, BatchFileObjects> batchFileObjectsMap, Map.Entry<String,Object[]> headerObj) throws IOException;
	
	public abstract void generateBatchReturnFile(String key, SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) throws IOException;
	
	public abstract ResponseEntity<String> deleteTempFile(Map<String, File>  downloadFileMap, HttpServletResponse response, String deleteTempFile, Long batchUploadId) throws IOException;
	
	public Map<String, File> generateFile(BatchReturnFile batchReturnFile, BatchReturnFileModel batchReturnFileModel, Map<String, BatchFileObjects> batchFileObjectsMap, String timeZone) throws IOException {
		Map<String, File> fileMap = new HashMap<>();
			
		for(Map.Entry<String,BatchFileObjects> batchFileObjectsEntry : batchFileObjectsMap.entrySet()) {
			List<SaleTransaction> result = batchReturnFileModel.getResult();
			try (CSVPrinter csvFilePrinterContent = new CSVPrinter(batchFileObjectsEntry.getValue().getFileWriter(), batchFileObjectsEntry.getValue().getCsvFileFormat());) {
				LOGGER.debug("SaleTransaction size for batch return file ={} ",result.size());
				// Write a new transaction object list to the CSV file
				for (SaleTransaction saleTransaction : result) {
					List<String> saleTransactionDataRecord = new ArrayList<>();
					batchReturnFile.generateBatchReturnFile(batchFileObjectsEntry.getKey(), saleTransaction, saleTransactionDataRecord, timeZone);
					if(!saleTransactionDataRecord.isEmpty()) {
						csvFilePrinterContent.printRecord(saleTransactionDataRecord);
					}
				}
				LOGGER.info("CSV file report has been created successfully !!!");
			}
			finally {
		    	if(batchFileObjectsEntry.getValue().getCsvFilePrinter()!=null) {
		    		batchFileObjectsEntry.getValue().getCsvFilePrinter().close();
		    	}
		    	if(batchFileObjectsEntry.getValue().getFileWriter()!=null) {
		    		batchFileObjectsEntry.getValue().getFileWriter().close();
		    	}
			}
			fileMap.put(batchFileObjectsEntry.getKey(), batchFileObjectsEntry.getValue().getFile());
		}
		
		return fileMap;
	}
	
	public Map<String, BatchFileObjects> createFileMap(BatchReturnFile batchReturnFile, Map<String, Object[]> fileHeadersMap, String legalEntityName) throws IOException {
		Map<String, BatchFileObjects> batchFileObjectsMap = new HashMap<>();
		String reportPath = propertyService.getPropertyValue(BluefinWebPortalConstants.TRANSACTIONREPORTPATH);
		LOGGER.info("legal Entity : ={}",legalEntityName);
		LOGGER.debug("reportPath for batch return file : ={}",reportPath);
		
		for(Map.Entry<String,Object[]> headerObj : fileHeadersMap.entrySet()) {
			batchReturnFile.createFile(fileHeadersMap,legalEntityName,reportPath,batchFileObjectsMap,headerObj);
		}
		
		return batchFileObjectsMap;
	}
}
