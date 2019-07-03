package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.bindb.service.TransationBinDBDetailsService;
import com.mcmcg.ico.bluefin.model.BatchReturnFileModel;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.repository.BatchUploadDAO;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.SaleTransactionDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.HttpsUtil;
import com.mcmcg.ico.bluefin.service.util.LoggingUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Service
public class BatchUploadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadService.class);

	@Autowired
	private BatchUploadDAO batchUploadDAO;
	@Autowired
	private SaleTransactionDAO saleTransactionDAO;
	@Autowired
	private PropertyService propertyService;
	@Autowired
    private LegalEntityAppService legalEntityAppService;
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private TransationBinDBDetailsService transationBinDBDetailsService;
	

	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	// CSV file header
	private static final Object[] FILE_HEADER = { "Batch Upload Id", "File Name", "Name", "Date Uploaded",
			"Batch Application", "Number Of Transactions", "Transactions Processed", "Approved Transactions",
			"Declined Transactions", "Error Transactions", "Rejected Transactions", "Process Start", "Process End",
			"UpLoadedBy" };

	public BatchUpload getBatchUploadById(Long id) {
		BatchUpload batchUpload = batchUploadDAO.findOne(id);

		if (batchUpload == null) {
			LOGGER.error("Unable to find batch upload with id = {}", id);
			throw new CustomNotFoundException(String.format("Unable to find batch upload with id = [%s]", id));
		}

		LegalEntityApp legalEntityApp = legalEntityAppDAO.findByLegalEntityAppId(batchUpload.getLegalEntityAppId());
		if (legalEntityApp == null) {
			LOGGER.debug(LoggingUtil.adminAuditInfo("Legal Entity name not found", BluefinWebPortalConstants.SEPARATOR,
					"Unable to find Legal Entity with Id : ", String.valueOf(batchUpload.getLegalEntityAppId())));
			batchUpload.setLegalEntityName("Not Found");
		}
		else {
			batchUpload.setLegalEntityName(legalEntityApp.getLegalEntityAppName());
		}
		
		return batchUpload;
	}

	public Iterable<BatchUpload> getAllBatchUploads(Integer page, Integer size, String sort) {
		Page<BatchUpload> result = batchUploadDAO
				.findAllByOrderByDateUploadedDesc(QueryDSLUtil.getPageRequest(page, size, sort));
		LOGGER.debug("BatchUpload result ={} ",result);
		if (page > result.getTotalPages() && page != 0) {
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		return result;
	}

	public Iterable<BatchUpload> getBatchUploadsFilteredByNoofdays(Integer page, Integer size, String sort,
			Integer noofdays) {
		DateTime dateBeforeNoofdays = new DateTime().toDateTime(DateTimeZone.UTC).minusDays(noofdays);
		Page<BatchUpload> result = batchUploadDAO.findByDateUploadedAfterOrderByDateUploadedDesc(dateBeforeNoofdays,
				QueryDSLUtil.getPageRequest(page, size, sort));
		LOGGER.debug("BatchUpload result: ={} ",result);
		if (page > result.getTotalPages() && page != 0) {
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		return result;
	}

	public BatchUpload createBatchUpload(String username, String fileName, String fileStream, int lines, String xAuthToken, String legalEntityName) {
		String batchProcessServiceUrl = propertyService.getPropertyValue("BATCH_PROCESS_SERVICE_URL");
		LOGGER.info("Creating new basic Batch Upload");
		LegalEntityApp legalEntityApp = legalEntityAppDAO.findByLegalEntityAppName(legalEntityName);
		BatchUpload batchUpload = createBasicBatchUpload(username, fileName, lines, legalEntityName, legalEntityApp.getLegalEntityAppId());
		batchUpload = batchUploadDAO.saveBasicBatchUpload(batchUpload);
		// call new application to process file content (fileStream)
		LOGGER.info("Calling ACF application to process file content");
		String response = HttpsUtil.sendPostRequest(batchProcessServiceUrl + batchUpload.getBatchUploadId().toString() +"/"+ legalEntityName,
				fileStream, xAuthToken);
		LOGGER.debug("ACF response ={} ",response);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JodaModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			LOGGER.info("Conveting ACF response into BatchUpload object");
			BatchUpload batchUploadResult = objectMapper.readValue(response, BatchUpload.class);
			batchUploadResult.setLegalEntityName(legalEntityName);
			return batchUploadResult;
		} catch (IOException e) {
			LOGGER.error("Unable to parse ACF batch process service response.", e);
			throw new CustomException("Unable to parse ACF batch process service response.");
		}
	}

	private BatchUpload createBasicBatchUpload(String username, String fileName, int lines, String legalEntityName, Long legalEntityAppId) {
		BatchUpload batchUpload = new BatchUpload();
		batchUpload.setDateUploaded(new DateTime().toDateTime(DateTimeZone.UTC));
		DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");
		String date = fmt.print(new DateTime().toDateTime(DateTimeZone.UTC));
		batchUpload.setName(date);
		batchUpload.setFileName(fileName);
		batchUpload.setUpLoadedBy(username);
		batchUpload.setBatchApplication("Latitude");
		batchUpload.setProcessStart(new DateTime().toDateTime(DateTimeZone.UTC));
		batchUpload.setNumberOfTransactions(lines);
		batchUpload.setLegalEntityName(legalEntityName);
		batchUpload.setLegalEntityAppId(legalEntityAppId);
		return batchUpload;
	}

	public File getBatchUploadsReport(Integer noofdays, String timeZone) throws IOException {
		List<BatchUpload> result;
		File file;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");
		LOGGER.debug("reportPath ={} ",reportPath);

		// Batch Upload Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (noofdays == null) {
			result = batchUploadDAO.findAll();
		} else {
			DateTime dateBeforeNoofdaysUTC = new DateTime().toDateTime(DateTimeZone.UTC).minusDays(noofdays);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateBeforeNoofdays = dateBeforeNoofdaysUTC.withZone(dtZone);
			result = batchUploadDAO.findByDateUploadedAfter(dateBeforeNoofdays);
		}

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		boolean flag;
		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, UUID.randomUUID() + ".csv");
			flag = file.createNewFile();
			if(flag) {
				LOGGER.info("Batch file Created {}", file.getName());
			}
		} catch (Exception e) {
			LOGGER.error("Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
		}
		// initialize FileWriter object
		try (FileWriter fileWriter = new FileWriter(file);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {

			// initialize CSVPrinter object

			// Create CSV file header
			csvFilePrinter.printRecord(FILE_HEADER);

			DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa");
			LOGGER.debug("BatchUpload result size ={} ",result.size());
			// Write a new transaction object list to the CSV file
			for (BatchUpload batchUpload : result) {
				List<String> batchUploadDataRecord = new ArrayList<>();
				batchUploadDataRecord
						.add(batchUpload.getBatchUploadId() == null ? " " : batchUpload.getBatchUploadId().toString());
				batchUploadDataRecord.add(batchUpload.getFileName());
				batchUploadDataRecord.add(batchUpload.getName());
				// Batch Upload Date/Time (user's local time)
				// The time zone (for example, "America/Costa_Rica" or
				// "America/Los_Angeles") is passed as a parameter
				// and applied to the UTC from the database.
				if (batchUpload.getDateUploaded() == null) {
					batchUploadDataRecord.add("");
				} else {
					DateTime dateTimeUTC = batchUpload.getDateUploaded().toDateTime(DateTimeZone.UTC);
					DateTimeZone dtZone = DateTimeZone.forID(timeZone);
					DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
					batchUploadDataRecord.add(fmt.print(dateTimeUser));
				}
				batchUploadDataRecord.add(batchUpload.getBatchApplication());
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfTransactions()));
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfTransactionsProcessed()));
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfApprovedTransactions()));
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfDeclinedTransactions()));
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfErrorTransactions()));
				batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfRejected()));

				// Batch Upload Date/Time (user's local time)
				// The time zone (for example, "America/Costa_Rica" or
				// "America/Los_Angeles") is passed as a parameter
				// and applied to the UTC from the database.
				if (batchUpload.getProcessStart() == null) {
					batchUploadDataRecord.add("");
				} else {
					DateTime dateTimeUTC = batchUpload.getProcessStart().toDateTime(DateTimeZone.UTC);
					DateTimeZone dtZone = DateTimeZone.forID(timeZone);
					DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
					batchUploadDataRecord.add(fmt.print(dateTimeUser));
				}

				// Batch Upload Date/Time (user's local time)
				// The time zone (for example, "America/Costa_Rica" or
				// "America/Los_Angeles") is passed as a parameter
				// and applied to the UTC from the database.
				if (batchUpload.getProcessEnd() == null) {
					batchUploadDataRecord.add("");
				} else {
					DateTime dateTimeUTC = batchUpload.getProcessEnd().toDateTime(DateTimeZone.UTC);
					DateTimeZone dtZone = DateTimeZone.forID(timeZone);
					DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
					batchUploadDataRecord.add(fmt.print(dateTimeUser));
				}

				batchUploadDataRecord.add(batchUpload.getUpLoadedBy());

				csvFilePrinter.printRecord(batchUploadDataRecord);
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}
		return file;
	}

	public BatchReturnFileModel getBatchUploadTransactionsReport(Long batchUploadId) {
		List<SaleTransaction> result;
		BatchUpload batchUpload = null;
		BatchReturnFileModel batchReturnFileModel = new BatchReturnFileModel();

		if (batchUploadId == null) {
			result = saleTransactionDAO.findAll();
		} else {
			result = saleTransactionDAO.findByBatchUploadId(batchUploadId);
			batchUpload = getBatchUploadById(batchUploadId);
		}
		
		for(SaleTransaction saleTransactioData : result) {
			if (saleTransactioData != null) {
				LOGGER.debug("Record found for transactionId: {}", saleTransactioData.getApplicationTransactionId());
				saleTransactioData.setBinDBDetails(transationBinDBDetailsService.fetchBinDBDetail(saleTransactioData.getCardNumberFirst6Char()));
			} else {
				LOGGER.debug("Sale Transaction Record not found");
			}
		}
		
		batchReturnFileModel.setResult(result);
		batchReturnFileModel.setBatchUpload(batchUpload);
		
		return batchReturnFileModel;
		
	}
	
	public ResponseEntity<String> deleteTempFile(File downloadFile, HttpServletResponse response, String deleteTempFile) {
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
	
	public boolean checkLegalEntityStatus(String legalEntityAppName) {
		LegalEntityApp legalEntityApp = legalEntityAppService.getLegalEntityAppName(legalEntityAppName);
		if(legalEntityApp == null) {
			throw new CustomException("Legal Entity not found, Please provide a valid Legal Entity Name");
		}
		
		return legalEntityAppService.getLegalEntityAppName(legalEntityAppName).getIsActive().intValue() == 0;
	}
}
