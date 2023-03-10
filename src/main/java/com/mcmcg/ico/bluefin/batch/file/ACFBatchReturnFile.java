package com.mcmcg.ico.bluefin.batch.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class ACFBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACFBatchReturnFile.class);
	
	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final Object[] TRANSACTIONS_ACF_FILE_HEADER = { "Date","Time","Invoice","Customer","Card Type","Card Number","Amount","Source",
			"Auth","AVS","CVV2","Error Code"};
	private static final Object[] TRANSACTIONS_ACF_ERROR_FILE_HEADER = { "Date","Time","Invoice","Customer","Card Type","Card Number","Amount","Source",
			"Error","AVS","CVV2","Error Code"};

	@Override
	public void generateBatchReturnFile(String key, SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) throws IOException {

		LOGGER.info("adding values for ACF batch returned file : ");
		//Error Message
		String errorMessage = saleTransaction.getInternalResponseDescription() + "("
				+ saleTransaction.getInternalResponseCode() + ")".replaceAll("\r", "").replaceAll("\n", "");
		
		if(BluefinWebPortalConstants.SUCCESS.equalsIgnoreCase(key) && "1".equalsIgnoreCase(saleTransaction.getInternalStatusCode())) {
			getDateTimeFormat(saleTransaction, saleTransactionDataRecord, timeZone);
			generateBatchFileData(saleTransaction, saleTransactionDataRecord, saleTransaction.getInternalResponseCode());
		}
		if(BluefinWebPortalConstants.DECLINED.equalsIgnoreCase(key) && "2".equalsIgnoreCase(saleTransaction.getInternalStatusCode())) {
			getDateTimeFormat(saleTransaction, saleTransactionDataRecord, timeZone);
			generateBatchFileData(saleTransaction, saleTransactionDataRecord, errorMessage);
		}
		if(BluefinWebPortalConstants.ERROR.equalsIgnoreCase(key) 
				&& !("1".equalsIgnoreCase(saleTransaction.getInternalStatusCode()) || "2".equalsIgnoreCase(saleTransaction.getInternalStatusCode()))) {
			getDateTimeFormat(saleTransaction, saleTransactionDataRecord, timeZone);
			generateBatchFileData(saleTransaction, saleTransactionDataRecord, errorMessage);
		}
		
		LOGGER.info("Values for ACF batch returned file added successfully");
	}

	@Override
	public Map<String, Object[]>  createFileHeader() {
		Map<String, Object[]> filesHeadersMap = new HashMap<>();
		filesHeadersMap.put(BluefinWebPortalConstants.SUCCESS, TRANSACTIONS_ACF_FILE_HEADER);
		filesHeadersMap.put(BluefinWebPortalConstants.DECLINED, TRANSACTIONS_ACF_ERROR_FILE_HEADER);
		filesHeadersMap.put(BluefinWebPortalConstants.ERROR, TRANSACTIONS_ACF_ERROR_FILE_HEADER);
		return filesHeadersMap;
	}
	
	@Override
	public ResponseEntity<String> deleteTempFile(Map<String, File> downloadFileMap, HttpServletResponse response, String deleteTempFile, Long batchUploadId, String legalEntityName) throws IOException {
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		response.setContentType(BluefinWebPortalConstants.ACFAPPOCTSTREAM);
	    response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, 
	    		BluefinWebPortalConstants.ATTACHMENTFILENAME+BluefinWebPortalConstants.BATCHRETURNFILES+batchUploadId+"_"+legalEntityName+"_"+ft.format(new Date())+".zip");
	    response.setStatus(HttpServletResponse.SC_OK);


        List<String> fileNames = new ArrayList<>();
        for(Map.Entry<String,File> files : downloadFileMap.entrySet()) {
        	fileNames.add(files.getValue().getPath());
        }

        LOGGER.info("Number of ACF files {} ",fileNames.size());

        FileSystemResource resource = null;
        InputStream inputStream = null;
	    try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
	        for (String file : fileNames) {
	            resource = new FileSystemResource(file);
	            inputStream = resource.getInputStream();
	            ZipEntry e = new ZipEntry(resource.getFilename());
	            e.setSize(resource.contentLength());
	            zippedOut.putNextEntry(e);
	            StreamUtils.copy(inputStream, zippedOut);
	            zippedOut.closeEntry();
	            inputStream.close();
	        }
	        zippedOut.finish();
	        for(String filePath : fileNames) {
	            Files.deleteIfExists(Paths.get(filePath));
	        }
		    
		    return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	        
	    } catch (Exception e) {
	    	LOGGER.error("An error occured to during download ACF return file= "+e);
			throw new CustomException("An error occured to during downloading ACF return file."+e.getMessage());
	    }
	    
	}
	
	@Override
	public Map<String, BatchFileObjects> createFile(Map<String, Object[]> fileHeadersMap, String legalEntityName, 
			String reportPath, Map<String, BatchFileObjects> batchFileObjectsMap, Map.Entry<String,Object[]> headerObj, Long batchUploadId) throws IOException {
		
		File file;
		boolean flag;
		String fileName = "";
		try {
			fileName = BluefinWebPortalConstants.BATCHRETURNFILENAMEFORACF+batchUploadId+"_"+legalEntityName+"_"+headerObj.getKey();
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, fileName + ".csv");
			flag = file.createNewFile();
			if(flag) {
				LOGGER.info("Batch return file Created  {}", file.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error creating batch return file : {}{}{}", reportPath, fileName, ".csv", e);
			throw new CustomException("Error creating file batch return file : " + reportPath + fileName + ".csv");
		}

		// Create CSV file header
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		// initialize FileWriter object
		FileWriter fileWriter = new FileWriter(file);
		try (CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat)) {
			csvFilePrinter.printRecord(headerObj.getValue());
	
			// Create the CSVFormat object with "\n" as a record delimiter
			csvFileFormat = CSVFormat.DEFAULT.withTrim();
			
			BatchFileObjects batchFileObjects = new BatchFileObjects();
			batchFileObjects.setFile(file);
			batchFileObjects.setCsvFileFormat(csvFileFormat);
			batchFileObjects.setFileWriter(fileWriter);
			batchFileObjects.setCsvFilePrinter(csvFilePrinter);
			
			batchFileObjectsMap.put(headerObj.getKey(), batchFileObjects);
		
	
			return batchFileObjectsMap;
		}
		
	}
	
	public void generateBatchFileData(SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String msg) {
		// Invoice
		saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

		BigDecimal amount = saleTransaction.getChargeAmount().setScale(2, BigDecimal.ROUND_DOWN);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
						
		//Customer Name
		String customerName = saleTransaction.getFirstName()+" "+saleTransaction.getLastName();
		if(StringUtils.isNotBlank(customerName)){
		customerName=customerName.replace(",", " ");
		}
		saleTransactionDataRecord.add(customerName);
						
		//Card Type
		String cardBrand = BluefinWebPortalConstants.CARDBRAND;
		if(saleTransaction.getBinDBDetails()!=null) {
			cardBrand = saleTransaction.getBinDBDetails().getBrand();
			
		}
		saleTransactionDataRecord.add(cardBrand);		
						
		//Card Number
		saleTransactionDataRecord.add(saleTransaction.getCardNumberLast4Char());
						
		//Amount
		saleTransactionDataRecord.add(saleTransaction.getChargeAmount() == null ? "" : df.format(amount));

		//Source
		saleTransactionDataRecord.add(BluefinWebPortalConstants.UPLOADED);
				
		//Message
		saleTransactionDataRecord.add(msg);
						
		//AVS
		saleTransactionDataRecord.add(" ");
						
		//CVV2
		saleTransactionDataRecord.add(" ");
						
		//Error Code
		saleTransactionDataRecord.add("0");
	}
	
	public SaleTransaction getDateTimeFormat(SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) {
		LOGGER.info("Entering to set date and time for batch return file - ACF");
		// TransactionDateTime needs to be split into two parts.
		DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
		DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm");

		// Batch Upload Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (saleTransaction.getTransactionDateTime() == null) {
			saleTransactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = saleTransaction.getTransactionDateTime().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			saleTransactionDataRecord.add(fmt1.print(dateTimeUser));
		}

		// Batch Upload Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (saleTransaction.getTransactionDateTime() == null) {
			saleTransactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = saleTransaction.getTransactionDateTime().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			saleTransactionDataRecord.add(fmt2.print(dateTimeUser));
		}
		LOGGER.info("Exiting after setting date and time for batch return file - ACF");
		return saleTransaction;
	}
}
