package com.mcmcg.ico.bluefin.batch.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.mcmcg.ico.bluefin.service.PropertyService;

@Component
public class ACFBatchReturnFile extends BatchReturnFile {

	private static final Logger LOGGER = LoggerFactory.getLogger(ACFBatchReturnFile.class);
	
	@Autowired
	private PropertyService propertyService; 
	
	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final Object[] TRANSACTIONS_ACF_FILE_HEADER = { "Date","Time","Invoice","Customer","Card Type","Card Number","Amount","Source",
			"Auth","AVS","CVV2","Error Code"};
	private static final Object[] TRANSACTIONS_ACF_ERROR_FILE_HEADER = { "Date","Time","Invoice","Customer","Card Type","Card Number","Amount","Source",
			"Error","AVS","CVV2","Error Code"};
	
	@Value("${spring.bluefin.mcm.legal.entity}")
	private String mcmLatitude;
	
	@Value("${spring.bluefin.acf.legal.entity}")
	private String acfLatitude;
	
	@Value("${spring.bluefin.jpf.legal.entity}")
	private String jpfLatitude;

	@Override
	public void generateBatchReturnFile(String key, SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String timeZone) throws IOException {

		LOGGER.info("adding values for ACF batch returned file : "+saleTransaction.getApplicationTransactionId());
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
	public ResponseEntity<String> deleteTempFile(Map<String, File> downloadFileMap, HttpServletResponse response, String deleteTempFile, Long batchUploadId) throws IOException {
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		response.setContentType(BluefinWebPortalConstants.ACFAPPOCTSTREAM);
	    response.setHeader(BluefinWebPortalConstants.CONTENTDISPOSITION, BluefinWebPortalConstants.ATTACHMENTFILENAME+BluefinWebPortalConstants.BATCHRETURNFILES+batchUploadId+"_"+ft.format(new Date())+".zip");
	    response.setStatus(HttpServletResponse.SC_OK);


        List<String> fileNames = new ArrayList<>();
        for(Map.Entry<String,File> files : downloadFileMap.entrySet()) {
        	fileNames.add(files.getValue().getPath());
        }

        LOGGER.info("Number of ACF files {} ",fileNames.size());

        FileSystemResource resource = null;
	    try (ZipOutputStream zippedOut = new ZipOutputStream(response.getOutputStream())) {
	        for (String file : fileNames) {
	            resource = new FileSystemResource(file);

	            ZipEntry e = new ZipEntry(resource.getFilename());
	            e.setSize(resource.contentLength());
	            zippedOut.putNextEntry(e);
	            StreamUtils.copy(resource.getInputStream(), zippedOut);
	            zippedOut.closeEntry();
	        }
	        zippedOut.finish();
	        zippedOut.close();
		    deleteFiles(new File(propertyService.getPropertyValue(BluefinWebPortalConstants.TRANSACTIONREPORTPATH)));
		    
		    return new ResponseEntity<>("{}", HttpStatus.NO_CONTENT);
	        
	    } catch (Exception e) {
	    	LOGGER.error("An error occured to during download ACF return file= "+e);
			throw new CustomException("An error occured to during downloading ACF return file.");
	    }	
	    finally {
	    	if(resource!=null) {
	    		try {
	    			resource.getInputStream().close();
				} catch (IOException e) {
					LOGGER.error("An error occured to close input stream= "+e);
				}
	    	}
	    }
	    
	}
	
	@Override
	public Map<String, BatchFileObjects> createFile(Map<String, Object[]> fileHeadersMap, String legalEntityName, 
			String reportPath, Map<String, BatchFileObjects> batchFileObjectsMap, Map.Entry<String,Object[]> headerObj) throws IOException {
		
		File file;
		boolean flag;
		String fileName = "";
		String legalEntityPrefix = jpfLatitude;
		if(legalEntityName.contains(acfLatitude)) {
			legalEntityPrefix = acfLatitude;
		}
		try {
			fileName = BluefinWebPortalConstants.BATCHRETURNFILENAMEFORACF+legalEntityPrefix+"_"+headerObj.getKey();
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
		@SuppressWarnings("resource")
		CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
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
	
	public void generateBatchFileData(SaleTransaction saleTransaction, List<String> saleTransactionDataRecord, String msg) {
		// Invoice
		saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

		BigDecimal amount = saleTransaction.getChargeAmount().setScale(2, BigDecimal.ROUND_DOWN);
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(0);
		df.setGroupingUsed(false);
						
		//Customer Name
		saleTransactionDataRecord.add(saleTransaction.getFirstName()+" "+saleTransaction.getLastName());
						
		//Card Type
		saleTransactionDataRecord.add(saleTransaction.getCardType());
						
		//Card Number
		String cardFirstDigits = StringUtils.isNotBlank(saleTransaction.getCardNumberFirst6Char()) ? saleTransaction.getCardNumberFirst6Char() : BluefinWebPortalConstants.CARDDIGITWITHSIXZERO;
		String cardLastDigits = StringUtils.isNotBlank(saleTransaction.getCardNumberLast4Char().replaceAll("XXXX-", "").replaceAll("null", "")) ? saleTransaction.getCardNumberLast4Char().replaceAll("XXXX-", "") : BluefinWebPortalConstants.CARDDIGITWITHFOURZERO;
		String cardMiddleDigits = "";
		String starsForCardMiddleDigits = BluefinWebPortalConstants.CARDDIGITWITHSIXZERO;
		if(StringUtils.isNotBlank(saleTransaction.getToken())) {
			cardMiddleDigits = saleTransaction.getToken().substring(6, saleTransaction.getToken().length()-4);
			starsForCardMiddleDigits = StringUtils.repeat("0", cardMiddleDigits.length());
		}
				
		saleTransactionDataRecord.add(cardFirstDigits+starsForCardMiddleDigits+cardLastDigits);
						
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
	
	public void deleteFiles(File f) throws IOException {
		File fList[] = f.listFiles();
		// Searchs .csv
		for (int i = 0; i < fList.length; i++) {
			File pes = fList[i];
			if (pes.getName().endsWith(".csv")) {
			    boolean success = pes.delete();
			    LOGGER.debug("File deleted for ACF ? {}",success);
			}
		}
	}
}
