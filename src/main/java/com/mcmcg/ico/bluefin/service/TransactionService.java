package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.mcmcg.ico.bluefin.model.TransactionType;
import com.mcmcg.ico.bluefin.persistent.LegalEntityApp;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.Transaction;
import com.mcmcg.ico.bluefin.persistent.User;
import com.mcmcg.ico.bluefin.persistent.jpa.RefundTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
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
            "Processor", "Application", "Origin", "Processor Transaction ID", "Transaction Date Time", "Approval Code",
            "Tokenized", "Payment Processor Status Code", "Payment Processor Status Code Description",
            "Payment Processor Response Code", "Payment Processor Response Code Description", "Internal Status Code",
            "Internal Status Description", "Internal Response Code", "Internal Response Description",
            "PaymentProcessorInternalStatusCodeID", "PaymentProcessorInternalResponseCodeID", "Date Created",
            "Account Period", "Desk", "Invoice Number", "User Defined Field 1", "User Defined Field 2",
            "User Defined Field 3" };

    @Autowired
    private SaleTransactionRepository saleTransactionRepository;
    @Autowired
    private VoidTransactionRepository voidTransactionRepository;
    @Autowired
    private RefundTransactionRepository refundTransactionRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${bluefin.wp.services.transactions.report.path}")
    private String reportPath;

    public Transaction getTransactionInformation(final String transactionId, TransactionType transactionType) {
        Transaction result = null;

        switch (transactionType) {
        case VOID:
            result = voidTransactionRepository.findByApplicationTransactionId(transactionId);
            break;
        case REFUND:
            result = refundTransactionRepository.findByApplicationTransactionId(transactionId);
            break;
        default:
            result = saleTransactionRepository.findByApplicationTransactionId(transactionId);
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }

    public Long countTransactionsWithPaymentProcessorRuleID(final Long paymentProcessorRuleId) {
        return saleTransactionRepository.countByPaymentProcessorRuleId(paymentProcessorRuleId);
    }

    public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
        Page<SaleTransaction> result;
        try {
            result = saleTransactionRepository.findTransaction(search, paging);
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
        User user = userRepository.findByUsername(username);
        List<LegalEntityApp> userLE = user.getLegalEntityApps();
        return userLE;
    }

    public File getTransactionsReport(String search) throws IOException {
        DateTimeFormatter fmt = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        List<SaleTransaction> result;

        File file = null;
        try {
            result = saleTransactionRepository.findTransactionsReport(search);
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
                transactionDataRecord
                        .add(transaction.getAmount() == null ? " " : "$" + transaction.getAmount().toString());
                transactionDataRecord.add(transaction.getLegalEntity());
                transactionDataRecord.add(transaction.getAccountNumber());
                transactionDataRecord.add(transaction.getApplicationTransactionId());
                transactionDataRecord.add(transaction.getMerchantId());
                transactionDataRecord.add(transaction.getProcessorName());
                transactionDataRecord.add(transaction.getApplication());
                transactionDataRecord.add(transaction.getOrigin());
                transactionDataRecord.add(transaction.getProcessorTransactionId());
                transactionDataRecord.add(fmt.print(transaction.getTransactionDateTime()));
                // Removed field: TestMode()
                transactionDataRecord.add(transaction.getApprovalCode());
                transactionDataRecord.add(transaction.getTokenized());
                transactionDataRecord.add(transaction.getPaymentProcessorStatusCode());
                transactionDataRecord.add(transaction.getPaymentProcessorStatusCodeDescription());
                transactionDataRecord.add(transaction.getProcessorResponseCode());
                transactionDataRecord.add(transaction.getProcessorResponseCodeDescription());
                transactionDataRecord.add(transaction.getInternalStatusCode());
                transactionDataRecord.add(transaction.getInternalStatusDescription());
                transactionDataRecord.add(transaction.getInternalResponseCode());
                transactionDataRecord.add(transaction.getInternalResponseDescription());
                transactionDataRecord.add(transaction.getPaymentProcessorInternalStatusCodeId() == null ? " "
                        : transaction.getPaymentProcessorInternalStatusCodeId().toString());
                transactionDataRecord.add(transaction.getPaymentProcessorInternalResponseCodeId() == null ? " "
                        : transaction.getPaymentProcessorInternalResponseCodeId().toString());
                transactionDataRecord.add(fmt.print(transaction.getCreatedDate()));
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
                csvFilePrinter.printRecord(transactionDataRecord);
                count++;
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }
        return file;
    }

}
