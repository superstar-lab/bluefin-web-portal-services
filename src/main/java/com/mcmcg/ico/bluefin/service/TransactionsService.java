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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
import com.mcmcg.ico.bluefin.persistent.jpa.TransactionRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.UserRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.VoidTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;

@Service
public class TransactionsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsService.class);

    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    // CSV file header
    private static final Object[] FILE_HEADER = { "#", "Bluefin transaction ID", "Processor transaction ID",
            "Transaction Date", "Account Number", "Amount", "Legal Entity App", "Card Number", "Card Type", "Customer",
            "Processor", "Status", "Transaction type" };

    @Autowired
    private TransactionRepository transactionRepository;
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
            result = transactionRepository.findByApplicationTransactionId(transactionId);
        }

        if (result == null) {
            throw new CustomNotFoundException("Transaction not found with id = [" + transactionId + "]");
        }

        return result;
    }

    public Iterable<SaleTransaction> getTransactions(String search, PageRequest paging) {
        Page<SaleTransaction> result;
        try {
            result = transactionRepository.findTransaction(search, paging);
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
        List<SaleTransaction> result;

        File file = null;
        try {
            result = transactionRepository.findTransactionsReport(search);
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
            LOGGER.error("Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv");
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
                transactionDataRecord.add(transaction.getApplicationTransactionId());
                transactionDataRecord.add(transaction.getProcessorTransactionId());
                transactionDataRecord.add(transaction.getTransactionDateTime() == null ? ""
                        : fmt.print(new DateTime(transaction.getTransactionDateTime())));
                transactionDataRecord.add(transaction.getAccountNumber());
                transactionDataRecord
                        .add(transaction.getAmount() == null ? "" : "$" + transaction.getAmount().toString());
                transactionDataRecord.add(transaction.getLegalEntity());
                transactionDataRecord.add(transaction.getCardNumberLast4Char());
                transactionDataRecord.add(transaction.getCardType());
                transactionDataRecord.add(transaction.getFirstName() + " " + transaction.getLastName());
                transactionDataRecord.add(transaction.getProcessorName());
                transactionDataRecord.add(
                        transaction.getInternalStatusCode() == null ? "" : transaction.getInternalStatusCode().name());
                transactionDataRecord.add(transaction.getTransactionType());
                csvFilePrinter.printRecord(transactionDataRecord);
                count++;
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }
        return file;
    }

}
