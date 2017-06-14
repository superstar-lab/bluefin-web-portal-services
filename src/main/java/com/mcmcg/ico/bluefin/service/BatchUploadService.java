package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.model.BatchUpload;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.StatusCode;
import com.mcmcg.ico.bluefin.repository.BatchUploadDAO;
import com.mcmcg.ico.bluefin.repository.SaleTransactionDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.HttpsUtil;
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

	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	// CSV file header
	private static final Object[] FILE_HEADER = { "Batch Upload Id", "File Name", "Name", "Date Uploaded",
			"Batch Application", "Number Of Transactions", "Transactions Processed", "Approved Transactions",
			"Declined Transactions", "Error Transactions", "Rejected Transactions", "Process Start", "Process End",
			"UpLoadedBy" };
	private static final Object[] TRANSACTIONS_FILE_HEADER = { "Date", "Time", "Invoice", "Amount", "Result",
			"Error Message" };

	public BatchUpload getBatchUploadById(Long id) {
		BatchUpload batchUpload = batchUploadDAO.findOne(id);

		if (batchUpload == null) {
			LOGGER.error("BatchUploadService :: getBatchUploadById() : Unable to find batch upload with id = {}", id);
			throw new CustomNotFoundException(String.format("Unable to find batch upload with id = [%s]", id));
		}

		return batchUpload;
	}

	public Iterable<BatchUpload> getAllBatchUploads(Integer page, Integer size, String sort) {
		Page<BatchUpload> result = batchUploadDAO
				.findAllByOrderByDateUploadedDesc(QueryDSLUtil.getPageRequest(page, size, sort));
		LOGGER.debug("BatchUploadService :: getAllBatchUploads() : BatchUpload result : "+result);
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
		LOGGER.debug("BatchUploadService :: getBatchUploadsFilteredByNoofdays() : BatchUpload result : "+result);
		if (page > result.getTotalPages() && page != 0) {
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		return result;
	}

	public BatchUpload createBatchUpload(String username, String fileName, String fileStream, int lines) {
		String batchProcessServiceUrl = propertyService.getPropertyValue("BATCH_PROCESS_SERVICE_URL");
		LOGGER.info("BatchUploadService :: createBatchUpload() : Creating new basic Batch Upload");
		BatchUpload batchUpload = createBasicBatchUpload(username, fileName, lines);
		batchUpload = batchUploadDAO.saveBasicBatchUpload(batchUpload);
		// call new application to process file content (fileStream)
		LOGGER.info("BatchUploadService :: createBatchUpload() : Calling ACF application to process file content");
		String response = HttpsUtil.sendPostRequest(batchProcessServiceUrl + batchUpload.getBatchUploadId().toString(),
				fileStream);
		LOGGER.debug("BatchUploadService :: createBatchUpload() : ACF response : "+response);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModule(new JodaModule());
			objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			LOGGER.info("BatchUploadService :: createBatchUpload() :Conveting ACF response into BatchUpload object");
			return objectMapper.readValue(response, BatchUpload.class);
		} catch (IOException e) {
			LOGGER.error("BatchUploadService :: createBatchUpload() : Unable to parse ACF batch process service response.", e);
			throw new CustomException("Unable to parse ACF batch process service response.");
		}
	}

	private BatchUpload createBasicBatchUpload(String username, String fileName, int lines) {
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
		return batchUpload;
	}

	public File getBatchUploadsReport(Integer noofdays, String timeZone) throws IOException {
		List<BatchUpload> result = null;
		File file = null;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");
		LOGGER.debug("BatchUploadService :: getBatchUploadsReport() : reportPath : "+reportPath);

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
		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, UUID.randomUUID() + ".csv");
			file.createNewFile();
		} catch (Exception e) {
			LOGGER.error("BatchUploadService :: getBatchUploadsReport() : Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
		}
		// initialize FileWriter object
		try (FileWriter fileWriter = new FileWriter(file);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {

			// initialize CSVPrinter object

			// Create CSV file header
			csvFilePrinter.printRecord(FILE_HEADER);

			DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa");
			LOGGER.debug("BatchUploadService :: getBatchUploadsReport() : BatchUpload result size : "+result.size());
			// Write a new transaction object list to the CSV file
			for (BatchUpload batchUpload : result) {
				List<String> batchUploadDataRecord = new ArrayList<String>();
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
				batchUploadDataRecord.add(batchUpload.getBatchApplication().toString());
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

	public File getBatchUploadTransactionsReport(Long batchUploadId, String timeZone) throws IOException {
		List<SaleTransaction> result = null;
		File file = null;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");
		LOGGER.debug("BatchUploadService :: getBatchUploadTransactionsReport() : reportPath : "+reportPath);

		if (batchUploadId == null) {
			result = saleTransactionDAO.findAll();
		} else {
			result = saleTransactionDAO.findByBatchUploadId(batchUploadId);
		}

		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, UUID.randomUUID() + ".csv");
			file.createNewFile();
		} catch (Exception e) {
			LOGGER.error("BatchUploadService :: getBatchUploadTransactionsReport() : Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
		}

		// Create CSV file header
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		// initialize FileWriter object
		FileWriter fileWriter = new FileWriter(file);
		@SuppressWarnings("resource")
		CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
		csvFilePrinter.printRecord(TRANSACTIONS_FILE_HEADER);

		// Create the CSVFormat object with "\n" as a record delimiter
		csvFileFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withRecordSeparator(NEW_LINE_SEPARATOR);

		try (CSVPrinter csvFilePrinterContent = new CSVPrinter(fileWriter, csvFileFormat);) {

			// TransactionDateTime needs to be split into two parts.
			DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
			DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm:ss.SSa");
			LOGGER.debug("BatchUploadService :: getBatchUploadTransactionsReport() : SaleTransaction size : "+result.size());
			// Write a new transaction object list to the CSV file
			for (SaleTransaction saleTransaction : result) {
				List<String> saleTransactionDataRecord = new ArrayList<String>();

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

				csvFilePrinterContent.printRecord(saleTransactionDataRecord);
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}

		return file;
	}
}
