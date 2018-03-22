package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.BinDBDetails;
import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.PaymentFrequency;
import com.mcmcg.ico.bluefin.model.PaymentProcessor;
import com.mcmcg.ico.bluefin.model.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.model.RemittanceSale;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.Transaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.repository.CustomSaleTransactionDAO;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorDAO;
import com.mcmcg.ico.bluefin.repository.PaymentProcessorRemittanceDAO;
import com.mcmcg.ico.bluefin.repository.PropertyDAO;
import com.mcmcg.ico.bluefin.repository.ReconciliationStatusDAO;
import com.mcmcg.ico.bluefin.repository.RefundTransactionDAO;
import com.mcmcg.ico.bluefin.repository.SaleTransactionDAO;
import com.mcmcg.ico.bluefin.repository.UserDAO;
import com.mcmcg.ico.bluefin.repository.UserLegalEntityAppDAO;
import com.mcmcg.ico.bluefin.repository.VoidTransactionDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
public class TransactionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
	private static final String FAILEDTOPROCESSDATEFORMATMSG = "Unable to process find transaction, due an error with date formatting";
	// Delimiter used in CSV file
	private static final String NEW_LINE_SEPARATOR = "\n";
	// CSV file header
	private static final Object[] FILE_HEADER = { "#", "First Name", "Last Name", "Process User", "Transaction Type",
			"Address 1", "Address 2", "City", "State", "Postal Code", "Country", "Card Number Last 4 Char", "Card Type",
			"Token", "Amount", "Legal Entity", "Account Number", "Application Transaction ID", "Merchant ID",
			"Processor", "Application", "Origin", "Payment Frequency", "Processor Transaction ID",
			"Transaction Date Time", "Approval Code", "Tokenized", "Payment Processor Status Code",
			"Payment Processor Status Code Description", "Payment Processor Response Code",
			"Payment Processor Response Code Description", "Internal Status Code", "Internal Status Description",
			"Internal Response Code", "Internal Response Description", "PaymentProcessorInternalStatusCodeID",
			"PaymentProcessorInternalResponseCodeID", "Date Created", "Account Period", "Desk", "Invoice Number",
			"User Defined Field 1", "User Defined Field 2", "User Defined Field 3", "Batch Upload ID",
			"BIN","Brand","Bank","Type","Level","ISO Country","WWW","Phone","Info"};

	private static final Object[] REMITTANCE_FILE_HEADER = { "#", "Bluefin Transaction ID", "Payment Processor",
			"Status", "Amount Difference", "Transaction Type", "Bluefin Account Number", "Bluefin Amount",
			"Bluefin Date/Time", "Remittance Transaction ID", "Remittance Account Number", "Remittance Amount",
			"Remittance Date/Time", "Card Type", "Card Number (last 4)", "Merchant ID", "Application" };

	@Autowired
	private SaleTransactionDAO saleTransactionDAO;
	@Autowired
	private VoidTransactionDAO voidTransactionDAO;
	@Autowired
	private RefundTransactionDAO refundTransactionDAO;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private PaymentProcessorDAO paymentProcessorDAO;
	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;
	@Autowired
	private PaymentProcessorRemittanceDAO paymentProcessorRemittanceDAO;
	@Autowired
	private PropertyDAO propertyDAO;
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;     
	@Autowired
	private UserLegalEntityAppDAO userLegalEntityAppDAO;
	
	@Autowired
	private CustomSaleTransactionDAO customSaleTransactionDAO;

	public Transaction getTransactionInformation(final String transactionId, TransactionTypeCode transactionType) {
		LOGGER.info("Entering to get Transaction Information");
		Transaction result;
		switch (transactionType) {
		case VOID:
			result = voidTransactionDAO.findByApplicationTransactionId(transactionId);
			break;
		case REFUND:
			result = refundTransactionDAO.findByApplicationTransactionId(transactionId);
			break;
		case REMITTANCE:
			result = getRemittanceSaleResult(transactionId);
			break;
		default:
			result = saleTransactionDAO.findByApplicationTransactionId(transactionId);
		}
		
		if (result == null) {
			throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
		}

		LOGGER.debug("Exiting from result ={} ",result);
		return result;
	}

	public Transaction getRemittanceSaleResult(String transactionIdVal) {
		Transaction tranResult;

		PaymentProcessorRemittance pprForTran = paymentProcessorRemittanceDAO.findByProcessorTransactionId(transactionIdVal);
		if (pprForTran == null) {
			pprForTran = new PaymentProcessorRemittance();
		}
		saleTransactionDAO.findByProcessorTransactionId(transactionIdVal);

		PaymentProcessorRemittance paymentProcessorRemittanceForTran = new PaymentProcessorRemittance();
		paymentProcessorRemittanceForTran.setPaymentProcessorRemittanceId(pprForTran.getPaymentProcessorRemittanceId());
		paymentProcessorRemittanceForTran.setDateCreated(pprForTran.getDateCreated());
		paymentProcessorRemittanceForTran.setReconciliationStatusId(pprForTran.getReconciliationStatusId());
		paymentProcessorRemittanceForTran.setReconciliationDate(pprForTran.getReconciliationDate()); 
		paymentProcessorRemittanceForTran.setPaymentMethod(pprForTran.getPaymentMethod());
		paymentProcessorRemittanceForTran.setTransactionAmount(pprForTran.getTransactionAmount());
		paymentProcessorRemittanceForTran.setTransactionType(pprForTran.getTransactionType()); 
		paymentProcessorRemittanceForTran.setTransactionTime(pprForTran.getTransactionTime()); 
		paymentProcessorRemittanceForTran.setAccountId(pprForTran.getAccountId()); 
		paymentProcessorRemittanceForTran.setApplication(pprForTran.getApplication());
		paymentProcessorRemittanceForTran.setProcessorTransactionId(pprForTran.getProcessorTransactionId()); 
		paymentProcessorRemittanceForTran.setMerchantId(pprForTran.getMerchantId()); 
		paymentProcessorRemittanceForTran.setTransactionSource(pprForTran.getTransactionSource()); 
		paymentProcessorRemittanceForTran.setFirstName(pprForTran.getFirstName());
		paymentProcessorRemittanceForTran.setLastName(pprForTran.getLastName()); 
		paymentProcessorRemittanceForTran.setRemittanceCreationDate(pprForTran.getRemittanceCreationDate()); 
		paymentProcessorRemittanceForTran.setPaymentProcessorId(pprForTran.getPaymentProcessorId()); 
		paymentProcessorRemittanceForTran.setReProcessStatus(null); 
		paymentProcessorRemittanceForTran.setEtlRunId(null); 
		paymentProcessorRemittanceForTran.setSaleAccountNumber(pprForTran.getSaleAccountNumber()); 
		paymentProcessorRemittanceForTran.setSaleAmount(pprForTran.getSaleAmount());

		tranResult = paymentProcessorRemittanceForTran;

		LOGGER.debug("Result : {}",tranResult);
		return tranResult;
	}

	public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
		Page<SaleTransaction> result;
		try {
			result = customSaleTransactionDAO.findTransaction(search, paging);
		} catch (ParseException e) {
			throw new CustomNotFoundException(FAILEDTOPROCESSDATEFORMATMSG);
		}
		final int page = paging.getPageNumber();

		if (page > result.getTotalPages() && page != 0) {
			LOGGER.error("Unable to find the page requested");
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		LOGGER.debug("result :={} ",result);
		return result;
	}

	public List<LegalEntityApp> getLegalEntitiesFromUser(String username) {
		User user = userDAO.findByUsername(username);
		LOGGER.debug("user ={} ", user == null ? null : user.getUserId());
		List<LegalEntityApp> list = new ArrayList<>();
		if (user != null) {
			for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
				long legalEntityAppId = userLegalEntityApp.getLegalEntityAppId();
				list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));
			}
		}
		LOGGER.debug("result list size={} ",list.size());
		return list;
	}

	private File createFileToPrepareReport(String reportPath){
		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			File file = new File(dir, UUID.randomUUID() + ".csv");
			file.createNewFile();
			return file;
		} catch (Exception e) {
			LOGGER.error("Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
			throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
		}
	}
	
	public File getTransactionsReport(String search, String timeZone) throws IOException {
		List<SaleTransaction> result;
		String reportPath = propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_PATH");

		LOGGER.debug("ReportPath : {}",reportPath);
		File file;
		try {
			result = customSaleTransactionDAO.findTransactionsReport(search);
		} catch (ParseException e) {
			throw new CustomNotFoundException(FAILEDTOPROCESSDATEFORMATMSG);
		}

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

		file = createFileToPrepareReport(reportPath);
		// initialize FileWriter object
		try (FileWriter fileWriter = new FileWriter(file);
			CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
			// initialize CSVPrinter object

			// Create CSV file header
			csvFilePrinter.printRecord(FILE_HEADER);
			Integer count = 1;
			LOGGER.debug("Result size : ",result.size());
			// Write a new transaction object list to the CSV file
			for (SaleTransaction transaction : result) {
				List<String> transactionDataRecord = prepareDataForTransactionReport(transaction,String.valueOf(count),timeZone);
				csvFilePrinter.printRecord(transactionDataRecord);
				count++;
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}
		return file;
	}

	private List<String> prepareDataForTransactionReport(SaleTransaction transaction,String count,String timeZone){
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa");
		List<String> transactionDataRecord = new ArrayList<>();
		transactionDataRecord.add(count);
		transactionDataRecord.add(transaction.getFirstName());
		transactionDataRecord.add(transaction.getLastName());
		transactionDataRecord.add(transaction.getProcessUser());
		transactionDataRecord.add(transaction.getTransactionType());
		transactionDataRecord.add(transaction.getAddress1());
		transactionDataRecord.add(transaction.getAddress2());
		transactionDataRecord.add(transaction.getCity());
		transactionDataRecord.add(transaction.getState());
		transactionDataRecord.add(transaction.getPostalCode());
		transactionDataRecord.add(transaction.getCountry());
		transactionDataRecord.add(transaction.getCardNumberLast4Char());
		transactionDataRecord.add(transaction.getCardType());
		transactionDataRecord.add(transaction.getToken());
		transactionDataRecord.add(
				transaction.getChargeAmount() == null ? " " : "$" + transaction.getChargeAmount().toString());
		transactionDataRecord.add(transaction.getLegalEntityApp());
		transactionDataRecord.add(transaction.getAccountId());
		transactionDataRecord.add(transaction.getApplicationTransactionId());
		transactionDataRecord.add(transaction.getMerchantId());
		transactionDataRecord.add(transaction.getProcessor());
		transactionDataRecord.add(transaction.getApplication());
		transactionDataRecord.add(transaction.getOrigin());
		transactionDataRecord.add(PaymentFrequency.getPaymentFrequency(transaction.getOrigin()).toString());
		transactionDataRecord.add(transaction.getProcessorTransactionId());
		// Transaction Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (transaction.getTransactionDateTime() == null) {
			transactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = transaction.getTransactionDateTime().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			transactionDataRecord.add(fmt.print(dateTimeUser));
		}
		// Removed field: TestMode()
		transactionDataRecord.add(transaction.getApprovalCode());
		transactionDataRecord.add(transaction.getTokenized().toString());
		transactionDataRecord.add(transaction.getPaymentProcessorStatusCode());
		transactionDataRecord.add(transaction.getPaymentProcessorStatusCodeDescription());
		transactionDataRecord.add(transaction.getPaymentProcessorResponseCode());
		transactionDataRecord.add(transaction.getPaymentProcessorResponseCodeDescription());
		transactionDataRecord.add(transaction.getInternalStatusCode());
		transactionDataRecord.add(transaction.getInternalStatusDescription());
		transactionDataRecord.add(transaction.getInternalResponseCode());
		transactionDataRecord.add(transaction.getInternalResponseDescription());
		transactionDataRecord.add(transaction.getPaymentProcessorInternalStatusCodeId() == null ? " "
				: transaction.getPaymentProcessorInternalStatusCodeId().toString());
		transactionDataRecord.add(transaction.getPaymentProcessorInternalResponseCodeId() == null ? " "
				: transaction.getPaymentProcessorInternalResponseCodeId().toString());
		// Creation Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (transaction.getDateCreated() == null) {
			transactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = transaction.getDateCreated().toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			transactionDataRecord.add(fmt.print(dateTimeUser));
		}
		// Removed fields: PaymentProcessorRuleId(),
		// RulePaymentProcessorId(), RuleCardType(),
		// RuleMaximumMonthlyAmount(), RuleNoMaximumMonthlyAmountFlag(),
		// RulePriority()
		transactionDataRecord.add(transaction.getAccountPeriod());
		transactionDataRecord.add(transaction.getDesk());
		transactionDataRecord.add(transaction.getInvoiceNumber());
		transactionDataRecord.add(transaction.getUserDefinedField1());
		transactionDataRecord.add(transaction.getUserDefinedField2());
		transactionDataRecord.add(transaction.getUserDefinedField3());
		transactionDataRecord
				.add(transaction.getBatchUploadId() == null ? " " : transaction.getBatchUploadId().toString());
		BinDBDetails binDBDetails = transaction.getBinDBDetails();
		if (binDBDetails == null) {
			binDBDetails = new BinDBDetails();
		}
		transactionDataRecord.add(binDBDetails.getBin());
		transactionDataRecord.add(binDBDetails.getBrand());
		transactionDataRecord.add(binDBDetails.getBank());
		transactionDataRecord.add(binDBDetails.getType());
		transactionDataRecord.add(binDBDetails.getLevel());
		transactionDataRecord.add(binDBDetails.getIsocountry());
		transactionDataRecord.add(binDBDetails.getWww());
		transactionDataRecord.add(binDBDetails.getPhone());
		transactionDataRecord.add(binDBDetails.getInfo());
		return transactionDataRecord;
	}
	
	/**
	 * Create CSV file for remittance.
	 * 
	 * @param search
	 * 
	 * @return CSV file
	 * 
	 * @throws IOException
	 */
	public File getRemittanceTransactionsReport(String search, String timeZone,boolean negate) throws IOException {
		List<RemittanceSale> result;
		String reportPath = propertyDAO.getPropertyValue("TRANSACTIONS_REPORT_PATH");
		LOGGER.debug("ReportPath : {}",reportPath);

		File file;
		try {
			/**
			 * Commenting below mehtod to call from reittance dao, due to having some conflicts between display data logic result on UI and report generation
			 * Now having same dao logic will eliminate those descrepencies
			 */
			result= customSaleTransactionDAO.findRemittanceSaleRefundTransactionsReport(search, negate);
		} catch (ParseException e) {
			throw new CustomNotFoundException(FAILEDTOPROCESSDATEFORMATMSG);
		}

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		
		file = createFileToPrepareReport(reportPath);
		// initialize FileWriter object
		try (FileWriter fileWriter = new FileWriter(file);
				CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {

			// Create PaymentProcessor hashmap
			Map<Long, String> paymentProcessorMap = new HashMap<>();
			List<PaymentProcessor> paymentProcessorList = paymentProcessorDAO.findAll();
			LOGGER.debug("PaymentProcessorList size : {} ",paymentProcessorList.size());
			for (PaymentProcessor pp : paymentProcessorList) {
				paymentProcessorMap.put(pp.getPaymentProcessorId(), pp.getProcessorName());
			}

			// Create ReconciliationStatus hashmap
			Map<Long, String> reconciliationStatusMap = new HashMap<>();
			List<ReconciliationStatus> reconciliationStatusList = reconciliationStatusDAO.findAll();
			LOGGER.debug("ReconciliationStatusList size : {}",reconciliationStatusList.size());
			for (ReconciliationStatus rs : reconciliationStatusList) {
				reconciliationStatusMap.put(rs.getReconciliationStatusId(), rs.getReconciliationStatusValue());
			}

			// initialize CSVPrinter object

			// Create CSV file header
			csvFilePrinter.printRecord(REMITTANCE_FILE_HEADER);
			Integer count = 1;
			LOGGER.debug("Result size : {}",result.size());
			// Write a new transaction object list to the CSV file
			for (RemittanceSale transaction : result) {
				
				List<String> transactionDataRecord = prepareRemiattanceReportDataPerRow(transaction,String.valueOf(count),timeZone,paymentProcessorMap,reconciliationStatusMap);
				csvFilePrinter.printRecord(transactionDataRecord);
				count++;
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}
		return file;
	}
	
	private List<String> prepareRemiattanceReportDataPerRow(RemittanceSale transaction,String count,String timeZone,Map<Long, String> paymentProcessorMap,Map<Long, String> reconciliationStatusMap){
		DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa");
		List<String> transactionDataRecord = new ArrayList<>();
		transactionDataRecord.add(count);

		// Sale information section
		// Bluefin Transaction ID
		transactionDataRecord.add(transaction.getSaleTransaction().getApplicationTransactionId());

		// Payment Processor
		String processorName = transaction.getSaleTransaction().getProcessor();
		if (processorName == null) {
			processorName = paymentProcessorMap
					.get(transaction.getPaymentProcessorRemittance().getPaymentProcessorId());
		}
		transactionDataRecord.add(processorName);

		// Status
		String status;
		Long reconciliationStatusId = transaction.getSaleTransaction().getReconciliationStatusId();
		if (reconciliationStatusId != null) {
			status = reconciliationStatusMap.get(reconciliationStatusId);
		} else {
			status = "";
		}
		transactionDataRecord.add(status);

		// Amount Difference
		BigDecimal amountDifference = null;
		BigDecimal saleAmount = transaction.getSaleTransaction().getChargeAmount();
		BigDecimal transactionAmount = transaction.getPaymentProcessorRemittance().getTransactionAmount();
		if (saleAmount != null && transactionAmount != null) {
			amountDifference = saleAmount.subtract(transactionAmount);
		}
		transactionDataRecord.add(amountDifference == null ? "" : "$" + amountDifference.toString());

		// Transaction Type
		String transactionType = transaction.getSaleTransaction().getTransactionType();
		getTransactionType(transactionType,transaction);
		transactionDataRecord.add(transactionType);

		// Bluefin information section
		// Bluefin Account Number
		transactionDataRecord.add(transaction.getSaleTransaction().getAccountId());

		// Bluefin Amount
		transactionDataRecord.add(transaction.getSaleTransaction().getChargeAmount() == null ? ""
				: "$" + transaction.getSaleTransaction().getChargeAmount().toString());

		// Bluefin Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (transaction.getSaleTransaction().getTransactionDateTime() == null) {
			transactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = transaction.getSaleTransaction().getTransactionDateTime()
					.toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			transactionDataRecord.add(fmt.print(dateTimeUser));
		}

		// Remittance information section
		// Remittance Transaction ID
		transactionDataRecord.add(transaction.getPaymentProcessorRemittance().getProcessorTransactionId());

		// Remittance Account Number
		transactionDataRecord.add(transaction.getPaymentProcessorRemittance().getAccountId());

		// Remittance Amount
		transactionDataRecord.add(transaction.getPaymentProcessorRemittance().getTransactionAmount() == null
				? "" : transaction.getPaymentProcessorRemittance().getTransactionAmount().toString());

		// Remittance Date/Time (user's local time)
		// The time zone (for example, "America/Costa_Rica" or
		// "America/Los_Angeles") is passed as a parameter
		// and applied to the UTC from the database.
		if (transaction.getPaymentProcessorRemittance().getTransactionTime() == null) {
			transactionDataRecord.add("");
		} else {
			DateTime dateTimeUTC = transaction.getPaymentProcessorRemittance().getTransactionTime()
					.toDateTime(DateTimeZone.UTC);
			DateTimeZone dtZone = DateTimeZone.forID(timeZone);
			DateTime dateTimeUser = dateTimeUTC.withZone(dtZone);
			transactionDataRecord.add(fmt.print(dateTimeUser));
		}

		// Sale information section
		// Card Type
		transactionDataRecord.add(transaction.getSaleTransaction().getCardType());

		// Card Number (last 4)
		transactionDataRecord.add(transaction.getSaleTransaction().getCardNumberLast4Char());

		// Merchant ID
		transactionDataRecord.add(transaction.getPaymentProcessorRemittance().getMerchantId());

		// Application
		transactionDataRecord.add(transaction.getPaymentProcessorRemittance().getApplication());
		return transactionDataRecord;
	}
	
	private String getTransactionType(String transactionType,RemittanceSale transaction){
		String transactionTypeVal;
		if (transactionType == null) {
			transactionTypeVal = transaction.getPaymentProcessorRemittance().getTransactionType();
		} else {
			transactionTypeVal = transactionType;
		}
		return transactionTypeVal;
	}
}
