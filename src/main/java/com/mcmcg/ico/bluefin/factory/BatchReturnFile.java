package com.mcmcg.ico.bluefin.factory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.util.ApplicationUtil;

public abstract class BatchReturnFile {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchReturnFile.class);
	
	@Autowired
	private PropertyService propertyService; 
	
	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	public abstract Object[] createFileHeader();
	
	public abstract void generateBatchReturnFile(BatchReturnFileModel batchReturnFileModel, BatchFileObjects batchFileObjects, SaleTransaction saleTransaction, List<String> saleTransactionDataRecord) throws IOException;
	
	public File generateFile(BatchReturnFile batchReturnFile, BatchReturnFileModel batchReturnFileModel, BatchFileObjects batchFileObjects, String timeZone) throws IOException {
		
		List<SaleTransaction> result = batchReturnFileModel.getResult();
		try (CSVPrinter csvFilePrinterContent = new CSVPrinter(batchFileObjects.getFileWriter(), batchFileObjects.getCsvFileFormat());) {
			LOGGER.debug("SaleTransaction size for ACF={} ",result.size());
			// Write a new transaction object list to the CSV file
			for (SaleTransaction saleTransaction : result) {
				List<String> saleTransactionDataRecord = new ArrayList<>();
				ApplicationUtil.getDateTimeFormat(saleTransaction, saleTransactionDataRecord, timeZone);
				batchReturnFile.generateBatchReturnFile(batchReturnFileModel, batchFileObjects, saleTransaction, saleTransactionDataRecord);
				csvFilePrinterContent.printRecord(saleTransactionDataRecord);
			}
			LOGGER.info("CSV file report was created for ACF successfully !!!");
		}
		finally {
	    	if(batchFileObjects.getCsvFilePrinter()!=null) {
	    		batchFileObjects.getCsvFilePrinter().close();
	    	}
	    	if(batchFileObjects.getFileWriter()!=null) {
	    		batchFileObjects.getFileWriter().close();
	    	}
		}
		
		return batchFileObjects.getFile();
	}
	
	public BatchFileObjects createFile(Object[] obj) throws IOException {
		File file;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");
		LOGGER.debug("reportPath for ACF: ={}",reportPath);
		
		boolean flag;
		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, UUID.randomUUID() + ".csv");
			flag = file.createNewFile();
			if(flag) {
				LOGGER.info("Batch file Created for ACF {}", file.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error creating file for ACF: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file for ACF: " + reportPath + UUID.randomUUID() + ".csv");
		}

		// Create CSV file header
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		// initialize FileWriter object
		FileWriter fileWriter = new FileWriter(file);
		@SuppressWarnings("resource")
		CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
		csvFilePrinter.printRecord(obj);

		// Create the CSVFormat object with "\n" as a record delimiter
		csvFileFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withRecordSeparator(NEW_LINE_SEPARATOR);
		
		BatchFileObjects batchFileObjects = new BatchFileObjects();
		batchFileObjects.setFile(file);
		batchFileObjects.setCsvFileFormat(csvFileFormat);
		batchFileObjects.setFileWriter(fileWriter);
		batchFileObjects.setCsvFilePrinter(csvFilePrinter);
		
		return batchFileObjects;
		
	}
	
}
