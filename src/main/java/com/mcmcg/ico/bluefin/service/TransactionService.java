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

import com.mcmcg.ico.bluefin.model.LegalEntityApp;
import com.mcmcg.ico.bluefin.model.PaymentFrequency;
import com.mcmcg.ico.bluefin.model.ReconciliationStatus;
import com.mcmcg.ico.bluefin.model.SaleTransaction;
import com.mcmcg.ico.bluefin.model.Transaction;
import com.mcmcg.ico.bluefin.model.TransactionType.TransactionTypeCode;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.model.UserLegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessor;
import com.mcmcg.ico.bluefin.persistent.PaymentProcessorRemittance;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRemittanceRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.PaymentProcessorRepository;
import com.mcmcg.ico.bluefin.repository.LegalEntityAppDAO;
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
			"User Defined Field 1", "User Defined Field 2", "User Defined Field 3", "Batch Upload ID" };

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
	private PaymentProcessorRepository paymentProcessorRepository;
	@Autowired
	private ReconciliationStatusDAO reconciliationStatusDAO;
	@Autowired
	private PaymentProcessorRemittanceRepository paymentProcessorRemittanceRepository;
	@Autowired
	private PropertyService propertyService;
	@Autowired
	private LegalEntityAppDAO legalEntityAppDAO;
	@Autowired
	private UserLegalEntityAppDAO userLegalEntityAppDAO;

	public Transaction getTransactionInformation(final String transactionId, TransactionTypeCode transactionType) {
		Transaction result = null;

		switch (transactionType) {
		case VOID:
			result = (Transaction) voidTransactionDAO.findByApplicationTransactionId(transactionId);
			break;
		case REFUND:
			result = (Transaction) refundTransactionDAO.findByApplicationTransactionId(transactionId);
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

		return result;
	}

	public Transaction getRemittanceSaleResult(String transactionId) {
		Transaction result = null;

		PaymentProcessorRemittance ppr = paymentProcessorRemittanceRepository
				.findByProcessorTransactionId(transactionId);
		if (ppr == null) {
			ppr = new PaymentProcessorRemittance();
		}
		SaleTransaction st = saleTransactionDAO.findByProcessorTransactionId(transactionId);
		if (st == null) {
			st = new SaleTransaction();
		}
		String processorName = null;
		if (ppr != null) {
			processorName = paymentProcessorRepository.findByPaymentProcessorId(ppr.getPaymentProcessorId())
					.getProcessorName();
		}
		/*
		 * Short tokenized = null; if (st != null) { String tokenizedStr =
		 * st.getTokenized(); if (tokenizedStr != null) { if
		 * (tokenizedStr.equalsIgnoreCase("No")) { tokenized = 0; } else {
		 * tokenized = 1; } } }
		 */
		Short tokenized = st.getTokenized();

		PaymentProcessorRemittance paymentProcessorRemittance = new PaymentProcessorRemittance(
				ppr.getPaymentProcessorRemittanceId(), ppr.getCreatedDate(), ppr.getReconciliationStatusId(),
				ppr.getReconciliationDate(), ppr.getPaymentMethod(), ppr.getTransactionAmount(),
				ppr.getTransactionType(), ppr.getTransactionTime(), ppr.getAccountId(), ppr.getApplication(),
				ppr.getProcessorTransactionId(), ppr.getMerchantId(), ppr.getTransactionSource(), ppr.getFirstName(),
				ppr.getLastName(), ppr.getRemittanceCreationDate(), ppr.getPaymentProcessorId(), processorName,
				st.getSaleTransactionId(), st.getTransactionType(), st.getLegalEntityApp(), st.getAccountId(),
				st.getApplicationTransactionId(), st.getProcessorTransactionId(), st.getMerchantId(),
				st.getTransactionDateTime(), st.getCardNumberFirst6Char(), st.getCardNumberLast4Char(),
				st.getCardType(), st.getChargeAmount(), st.getExpiryDate(), st.getFirstName(), st.getLastName(),
				st.getAddress1(), st.getAddress2(), st.getCity(), st.getState(), st.getPostalCode(), st.getCountry(),
				st.getTestMode(), st.getToken(), tokenized, st.getPaymentProcessorResponseCode(),
				st.getPaymentProcessorResponseCodeDescription(), st.getApprovalCode(), st.getInternalResponseCode(),
				st.getInternalResponseDescription(), st.getInternalStatusCode(), st.getInternalStatusDescription(),
				st.getPaymentProcessorStatusCode(), st.getPaymentProcessorStatusCodeDescription(),
				st.getPaymentProcessorRuleId(), st.getRulePaymentProcessorId(), st.getRuleCardType(),
				st.getRuleMaximumMonthlyAmount(), st.getRuleNoMaximumMonthlyAmountFlag(), st.getRulePriority(),
				st.getProcessUser(), st.getProcessor(), st.getApplication(), st.getOrigin(), st.getAccountPeriod(),
				st.getDesk(), st.getInvoiceNumber(), st.getUserDefinedField1(), st.getUserDefinedField2(),
				st.getUserDefinedField3(), st.getDateCreated(), st.getIsVoided(), st.getIsRefunded(),
				st.getPaymentProcessorInternalStatusCodeId(), st.getPaymentProcessorInternalResponseCodeId(),
				st.getReconciliationStatusId(), st.getReconciliationDate(), st.getBatchUploadId(), "", "", "");

		result = (Transaction) paymentProcessorRemittance;

		return result;
	}

	public Long countTransactionsWithPaymentProcessorRuleID(final Long paymentProcessorRuleId) {
		return saleTransactionDAO.countByPaymentProcessorRuleId(paymentProcessorRuleId);
	}

	public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
		Page<SaleTransaction> result;
		try {
			result = saleTransactionDAO.findTransaction(search, paging);
		} catch (ParseException e) {
			throw new CustomNotFoundException("Unable to process find transaction, due an error with date formatting");
		}
		final int page = paging.getPageNumber();

		if (page > result.getTotalPages() && page != 0) {
			LOGGER.error("Unable to find the page requested");
			throw new CustomNotFoundException("Unable to find the page requested");
		}

		return result;
	}

	public List<LegalEntityApp> getLegalEntitiesFromUser(String username) {
		User user = userDAO.findByUsername(username);
		List<LegalEntityApp> list = new ArrayList<LegalEntityApp>();
		for (UserLegalEntityApp userLegalEntityApp : userLegalEntityAppDAO.findByUserId(user.getUserId())) {
			long legalEntityAppId = userLegalEntityApp.getUserLegalEntityAppId();
			list.add(legalEntityAppDAO.findByLegalEntityAppId(legalEntityAppId));

		}

		return list;
	}

	public File getTransactionsReport(String search, String timeZone) throws IOException {
		List<SaleTransaction> result;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");

		File file = null;
		try {
			result = saleTransactionDAO.findTransactionsReport(search);
		} catch (ParseException e) {
			throw new CustomNotFoundException("Unable to process find transaction, due an error with date formatting");
		}

		// Create the CSVFormat object with "\n" as a record delimiter
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

		try {
			File dir = new File(reportPath);
			dir.mkdirs();
			file = new File(dir, UUID.randomUUID() + ".csv");
			file.createNewFile();
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
			Integer count = 1;
			// Write a new transaction object list to the CSV file
			for (SaleTransaction transaction : result) {
				List<String> transactionDataRecord = new ArrayList<String>();
				transactionDataRecord.add(count.toString());
				// Removed field: SaleTransactionId();
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
				// Removed field: CardNumberFirst6Char());
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
				// transactionDataRecord.add(transaction.getPaymentFrequency());
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
				csvFilePrinter.printRecord(transactionDataRecord);
				count++;
			}
			LOGGER.info("CSV file report was created successfully !!!");
		}
		return file;
	}

	/**
	 * Get remittance, sale, refund, and void transactions. This will be one
	 * column of the UI.
	 * 
	 * @param search
	 * @param paging
	 * @param negate
	 * 
	 * @return list of objects containing these transactions
	 */
	public Iterable<PaymentProcessorRemittance> getRemittanceSaleRefundVoidTransactions(String search,
			PageRequest paging, boolean negate) {
		Page<PaymentProcessorRemittance> result;
		// Fix this when working on PaymentProcessorRemittance.
		/*
		 * try { result =
		 * saleTransactionDAO.findRemittanceSaleRefundTransactions(search,
		 * paging, negate); } catch (ParseException e) { throw new
		 * CustomNotFoundException(
		 * "Unable to process find remittance, sale, refund or void transactions, due an error with date formatting"
		 * ); } final int page = paging.getPageNumber();
		 * 
		 * if (page > result.getTotalPages() && page != 0) {
		 * LOGGER.error("Unable to find the page requested"); throw new
		 * CustomNotFoundException("Unable to find the page requested"); }
		 * 
		 * return result;
		 */
		return null;
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
	public File getRemittanceTransactionsReport(String search, String timeZone) throws IOException {
		List<PaymentProcessorRemittance> result;
		String reportPath = propertyService.getPropertyValue("TRANSACTIONS_REPORT_PATH");

		// Fix this when working on PaymentProcessorRemittance.
		/*
		 * File file = null; try { result =
		 * saleTransactionDAO.findRemittanceSaleRefundTransactionsReport(search)
		 * ; } catch (ParseException e) { throw new
		 * CustomNotFoundException("Unable to process find transaction, due an error with date formatting"
		 * ); }
		 * 
		 * // Create the CSVFormat object with "\n" as a record delimiter
		 * CSVFormat csvFileFormat =
		 * CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		 * 
		 * try { File dir = new File(reportPath); dir.mkdirs(); file = new
		 * File(dir, UUID.randomUUID() + ".csv"); file.createNewFile(); } catch
		 * (Exception e) { LOGGER.error("Error creating file: {}{}{}",
		 * reportPath, UUID.randomUUID(), ".csv", e); throw new
		 * CustomException("Error creating file: " + reportPath +
		 * UUID.randomUUID() + ".csv"); } // initialize FileWriter object try
		 * (FileWriter fileWriter = new FileWriter(file); CSVPrinter
		 * csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
		 * 
		 * // Create PaymentProcessor hashmap Map<Long, String>
		 * paymentProcessorMap = new HashMap<Long, String>();
		 * List<PaymentProcessor> paymentProcessorList =
		 * paymentProcessorRepository.findAll(); for (PaymentProcessor pp :
		 * paymentProcessorList) {
		 * paymentProcessorMap.put(pp.getPaymentProcessorId(),
		 * pp.getProcessorName()); }
		 * 
		 * // Create ReconciliationStatus hashmap Map<Long, String>
		 * reconciliationStatusMap = new HashMap<Long, String>();
		 * List<ReconciliationStatus> reconciliationStatusList =
		 * reconciliationStatusDAO.findAll(); for (ReconciliationStatus rs :
		 * reconciliationStatusList) {
		 * reconciliationStatusMap.put(rs.getReconciliationStatusId(),
		 * rs.getReconciliationStatus()); }
		 * 
		 * // initialize CSVPrinter object
		 * 
		 * // Create CSV file header
		 * csvFilePrinter.printRecord(REMITTANCE_FILE_HEADER);
		 * 
		 * DateTimeFormatter fmt =
		 * DateTimeFormat.forPattern("MM/dd/yyyy hh:mm:ss.SSa"); Integer count =
		 * 1; // Write a new transaction object list to the CSV file for
		 * (PaymentProcessorRemittance transaction : result) { List<String>
		 * transactionDataRecord = new ArrayList<String>();
		 * transactionDataRecord.add(count.toString());
		 * 
		 * // Sale information section // Bluefin Transaction ID
		 * transactionDataRecord.add(transaction.getSaleApplicationTransactionId
		 * ());
		 * 
		 * // Payment Processor String processorName =
		 * transaction.getSaleProcessorName(); if (processorName == null) {
		 * processorName =
		 * paymentProcessorMap.get(transaction.getPaymentProcessorId()); }
		 * transactionDataRecord.add(processorName);
		 * 
		 * // Status String status = null; Long reconciliationStatusId =
		 * transaction.getSaleReconciliationStatusId(); if
		 * (reconciliationStatusId != null) { status =
		 * reconciliationStatusMap.get(reconciliationStatusId); } else { status
		 * = ""; } transactionDataRecord.add(status);
		 * 
		 * // Amount Difference BigDecimal amountDifference = null; BigDecimal
		 * saleAmount = transaction.getSaleAmount(); BigDecimal
		 * transactionAmount = transaction.getTransactionAmount(); if
		 * (saleAmount != null && transactionAmount != null) { amountDifference
		 * = saleAmount.subtract(transactionAmount); }
		 * transactionDataRecord.add(amountDifference == null ? "" : "$" +
		 * amountDifference.toString());
		 * 
		 * // Transaction Type String transactionType =
		 * transaction.getSaleTransactionType(); if (transactionType == null) {
		 * transactionType = transaction.getTransactionType(); }
		 * transactionDataRecord.add(transactionType);
		 * 
		 * // Bluefin information section // Bluefin Account Number
		 * transactionDataRecord.add(transaction.getSaleAccountNumber());
		 * 
		 * // Bluefin Amount transactionDataRecord
		 * .add(transaction.getSaleAmount() == null ? "" : "$" +
		 * transaction.getSaleAmount().toString());
		 * 
		 * // Bluefin Date/Time (user's local time) // The time zone (for
		 * example, "America/Costa_Rica" or // "America/Los_Angeles") is passed
		 * as a parameter // and applied to the UTC from the database. if
		 * (transaction.getSaleTransactionDateTime() == null) {
		 * transactionDataRecord.add(""); } else { DateTime dateTimeUTC =
		 * transaction.getSaleTransactionDateTime().toDateTime(DateTimeZone.UTC)
		 * ; DateTimeZone dtZone = DateTimeZone.forID(timeZone); DateTime
		 * dateTimeUser = dateTimeUTC.withZone(dtZone);
		 * transactionDataRecord.add(fmt.print(dateTimeUser)); }
		 * 
		 * // Remittance information section // Remittance Transaction ID
		 * transactionDataRecord.add(transaction.getProcessorTransactionId());
		 * 
		 * // Remittance Account Number
		 * transactionDataRecord.add(transaction.getAccountId());
		 * 
		 * // Remittance Amount
		 * transactionDataRecord.add(transaction.getTransactionAmount() == null
		 * ? "" : transaction.getTransactionAmount().toString());
		 * 
		 * // Remittance Date/Time (user's local time) // The time zone (for
		 * example, "America/Costa_Rica" or // "America/Los_Angeles") is passed
		 * as a parameter // and applied to the UTC from the database. if
		 * (transaction.getTransactionTime() == null) {
		 * transactionDataRecord.add(""); } else { DateTime dateTimeUTC =
		 * transaction.getTransactionTime().toDateTime(DateTimeZone.UTC);
		 * DateTimeZone dtZone = DateTimeZone.forID(timeZone); DateTime
		 * dateTimeUser = dateTimeUTC.withZone(dtZone);
		 * transactionDataRecord.add(fmt.print(dateTimeUser)); }
		 * 
		 * // Sale information section // Card Type
		 * transactionDataRecord.add(transaction.getSaleCardType());
		 * 
		 * // Card Number (last 4)
		 * transactionDataRecord.add(transaction.getSaleCardNumberLast4Char());
		 * 
		 * // Merchant ID transactionDataRecord.add(transaction.getMID());
		 * 
		 * // Application
		 * transactionDataRecord.add(transaction.getApplication());
		 * 
		 * csvFilePrinter.printRecord(transactionDataRecord); count++; }
		 * LOGGER.info("CSV file report was created successfully !!!"); } return
		 * file;
		 */
		return null;
	}
}
