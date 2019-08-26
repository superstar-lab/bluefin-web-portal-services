package com.mcmcg.ico.bluefin.batch.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.factory.BatchReturnFile;
import com.mcmcg.ico.bluefin.model.BatchFileObjects;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;

@Component
public class MCMBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(MCMBatchReturnFile.class);
	
	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final Object[] TRANSACTIONS_MCM_FILE_HEADER = { "Date", "Time", "Invoice", "Amount", "Result",
	"Error Message" };
	
	@Override
	public void generateBatchReturnFile(String key, SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) throws IOException {

		LOGGER.info("adding values for MCM batch returned file");
		//Date and Time
		getDateTimeFormat(saleTransaction, saleTransactionDataRecord, timeZone);
		
		// Invoice
		saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

		BigDecimal amount = saleTransaction.getChargeAmount().setScale(2, BigDecimal.ROUND_DOWN);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);

		// Amount
		saleTransactionDataRecord.add(saleTransaction.getChargeAmount() == null ? "" : df.format(amount));

		// Result
		saleTransactionDataRecord.add(StatusCode.getStatusCode(saleTransaction.getInternalStatusCode()));

		// Error Message
		String errorMessage = saleTransaction.getInternalResponseDescription() + "("
				+ saleTransaction.getInternalResponseCode() + ")";
		saleTransactionDataRecord.add(errorMessage.replaceAll("\r", "").replaceAll("\n", ""));
		
		LOGGER.info("Values for MCM batch returned file added successfully");
	}

	@Override
	public Map<String, Object[]>  createFileHeader() {
		Map<String, Object[]> filesHeadersMap = new HashMap<>();
		filesHeadersMap.put(BluefinWebPortalConstants.SUCCESS, TRANSACTIONS_MCM_FILE_HEADER);
		return filesHeadersMap;
	}

	@Override
	public ResponseEntity<String> deleteTempFile(Map<String, File> downloadFileMap, HttpServletResponse response, String deleteTempFile, Long batchUploadId, String legalEntityName) {
		File downloadFile = downloadFileMap.get("success");
		InputStream inputStream = null;
		try {
			inputStream = FileUtils.openInputStream(downloadFile);
			response.setContentType(BluefinWebPortalConstants.APPOCTSTREAM);
			response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME + downloadFile.getName());
			
			FileCopyUtils.copy(inputStream, response.getOutputStream());
			LOGGER.debug(deleteTempFile, downloadFile.getName());
			boolean deleted = downloadFile.delete();
			LOGGER.debug("File deleted ? {}",deleted);
			return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
		} catch(Exception e) {
			LOGGER.error("An error occured to during getRemittanceTransactionsReport file= "+e);
			throw new CustomException("An error occured to during getRemittanceTransactionsReport file.");
		}
		finally {
		    	if(inputStream!=null) {
		    		try {
		    			inputStream.close();
					} catch (IOException e) {
						LOGGER.error("An error occured to close input stream= "+e);
					}
		    	}
		}
		
	}

	@Override
	public Map<String, BatchFileObjects> createFile(Map<String, Object[]> fileHeadersMap, String legalEntityName, 
			String reportPath, Map<String, BatchFileObjects> batchFileObjectsMap, Map.Entry<String,Object[]> headerObj, Long batchUploadId) throws IOException {
		
		File file;
		boolean flag;
		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, BluefinWebPortalConstants.BATCHRETURNFILENAMEFORACF+batchUploadId+"_"+legalEntityName + ".csv");
			flag = file.createNewFile();
			if(flag) {
				LOGGER.info("Batch return file Created  {}", file.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error creating batch return file : {}{}{}", reportPath, BluefinWebPortalConstants.BATCHRETURNFILENAMEFORACF+legalEntityName, ".csv", e);
			throw new CustomException("Error creating file batch return file : " + reportPath + UUID.randomUUID() + ".csv");
		}

		// Create CSV file header
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		// initialize FileWriter object
		FileWriter fileWriter = new FileWriter(file);
		@SuppressWarnings("resource")
		CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
		csvFilePrinter.printRecord(headerObj.getValue());

		// Create the CSVFormat object with "\n" as a record delimiter
		csvFileFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withRecordSeparator(NEW_LINE_SEPARATOR);
			
		BatchFileObjects batchFileObjects = new BatchFileObjects();
		batchFileObjects.setFile(file);
		batchFileObjects.setCsvFileFormat(csvFileFormat);
		batchFileObjects.setFileWriter(fileWriter);
		batchFileObjects.setCsvFilePrinter(csvFilePrinter);
		
		batchFileObjectsMap.put(headerObj.getKey(), batchFileObjects);
		
		return batchFileObjectsMap;
		
	}
	
	public SaleTransaction getDateTimeFormat(SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) {
		LOGGER.info("Entering to set date and time for batch return file - MCM");
		// TransactionDateTime needs to be split into two parts.
		DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
		DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm:ss.SSa");

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
		LOGGER.info("Exiting after setting date and time for batch return file - MCM");
		return saleTransaction;
	}
	
}
