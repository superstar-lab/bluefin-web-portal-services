package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.mcmcg.ico.bluefin.persistent.BatchUpload;
import com.mcmcg.ico.bluefin.persistent.SaleTransaction;
import com.mcmcg.ico.bluefin.persistent.jpa.BatchUploadRepository;
import com.mcmcg.ico.bluefin.persistent.jpa.SaleTransactionRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.HttpsUtil;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Service
public class BatchUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadService.class);

    @Autowired
    private BatchUploadRepository batchUploadRepository;
    @Autowired
    private SaleTransactionRepository saleTransactionRepository;
    @Autowired
    private InternalStatusCodeService internalStatusCodeService;

    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    // CSV file header
    private static final Object[] FILE_HEADER = { "Batch Upload Id", "File Name", "Name", "Date Uploaded",
            "Batch Application", "Number Of Transactions", "Transactions Processed", "Approved Transactions",
            "Declined Transactions", "Error Transactions", "Rejected Transactions", "Process Start", "Process End",
            "UpLoadedBy" };
    private static final Object[] TRANSACTIONS_FILE_HEADER = { "Date", "Time", "Invoice", "Amount", "Result",
            "Error Message" };

    @Value("${bluefin.wp.services.batch.upload.report.path}")
    private String reportPath;
    @Value("${bluefin.wp.services.batch.process.service.url}")
    private String batchProcessServiceUrl;

    public BatchUpload getBatchUploadById(Long id) {
        BatchUpload batchUpload = batchUploadRepository.findOne(id);

        if (batchUpload == null) {
            LOGGER.error("Unable to find batch upload with id = {}", id);
            throw new CustomNotFoundException(String.format("Unable to find batch upload with id = [%s]", id));
        }

        return batchUpload;
    }

    public Iterable<BatchUpload> getAllBatchUploads(Integer page, Integer size, String sort) {
        Page<BatchUpload> result = batchUploadRepository
                .findAllByOrderByDateUploadedDesc(QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }

    public Iterable<BatchUpload> getBatchUploadsFilteredByNoofdays(Integer page, Integer size, String sort,
            Integer noofdays) {
        DateTime dateBeforeNoofdays = new DateTime().toDateTime(DateTimeZone.UTC).minusDays(noofdays);
        Page<BatchUpload> result = batchUploadRepository.findByDateUploadedAfterOrderByDateUploadedDesc(
                dateBeforeNoofdays, QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }

    public BatchUpload createBatchUpload(String username, String fileName, String fileStream, int lines) {
        LOGGER.info("Creating new basic Batch Upload");
        BatchUpload batchUpload = createBasicBatchUpload(username, fileName, lines);
        batchUpload = batchUploadRepository.save(batchUpload);
        // call new application to process file content (fileStream)
        LOGGER.info("Calling ACF application to process file content");
        String response = HttpsUtil.sendPostRequest(batchProcessServiceUrl + batchUpload.getBatchUploadId().toString(),
                fileStream);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JodaModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            LOGGER.info("Conveting ACF response into BatchUpload object");
            return objectMapper.readValue(response, BatchUpload.class);
        } catch (IOException e) {
            LOGGER.error("Unable to parse ACF batch process service response.", e);
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

    public File getBatchUploadsReport(Integer noofdays) throws IOException {
        List<BatchUpload> result = null;
        File file = null;

        if (noofdays == null) {
            result = batchUploadRepository.findAll();
        } else {
            DateTime dateBeforeNoofdays = new DateTime().toDateTime(DateTimeZone.UTC).minusDays(noofdays);
            result = batchUploadRepository.findByDateUploadedAfter(dateBeforeNoofdays);
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
            // Write a new transaction object list to the CSV file
            for (BatchUpload batchUpload : result) {
                List<String> batchUploadDataRecord = new ArrayList<String>();
                batchUploadDataRecord
                        .add(batchUpload.getBatchUploadId() == null ? " " : batchUpload.getBatchUploadId().toString());
                batchUploadDataRecord.add(batchUpload.getFileName());
                batchUploadDataRecord.add(batchUpload.getName());
                batchUploadDataRecord.add(batchUpload.getDateUploaded() == null ? ""
                        : fmt.print(batchUpload.getDateUploaded().toDateTime(DateTimeZone.UTC)));
                batchUploadDataRecord.add(batchUpload.getBatchApplication().toString());
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfTransactions()));
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfTransactionsProcessed()));
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfApprovedTransactions()));
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfDeclinedTransactions()));
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfErrorTransactions()));
                batchUploadDataRecord.add(Integer.toString(batchUpload.getNumberOfRejected()));

                batchUploadDataRecord.add(batchUpload.getProcessStart() == null ? ""
                        : fmt.print(batchUpload.getProcessStart().toDateTime(DateTimeZone.UTC)));
                batchUploadDataRecord.add(batchUpload.getProcessEnd() == null ? ""
                        : fmt.print(batchUpload.getProcessEnd().toDateTime(DateTimeZone.UTC)));

                batchUploadDataRecord.add(batchUpload.getUpLoadedBy());

                csvFilePrinter.printRecord(batchUploadDataRecord);
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }
        return file;
    }

    public File getBatchUploadTransactionsReport(Long batchUploadId) throws IOException {
        List<SaleTransaction> result = null;
        File file = null;

        if (batchUploadId == null) {
            result = saleTransactionRepository.findAll();
        } else {
            result = saleTransactionRepository.findByBatchUploadId(batchUploadId);
        }

        try {
            File dir = new File(reportPath);
            dir.mkdirs();
            file = new File(dir, UUID.randomUUID() + ".csv");
            file.createNewFile();
        } catch (Exception e) {
            LOGGER.error("Error creating file: {}{}{}", reportPath, UUID.randomUUID(), ".csv", e);
            throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
        }

        // Create CSV file header
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        // initialize FileWriter object
        FileWriter fileWriter = new FileWriter(file);
        CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
        csvFilePrinter.printRecord(TRANSACTIONS_FILE_HEADER);

        // Create the CSVFormat object with "\n" as a record delimiter
        csvFileFormat = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL).withRecordSeparator(NEW_LINE_SEPARATOR);

        try (CSVPrinter csvFilePrinterContent = new CSVPrinter(fileWriter, csvFileFormat);) {

            // TransactionDateTime needs to be split into two parts.
            DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
            DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm:ss.SSa");
            // Write a new transaction object list to the CSV file
            for (SaleTransaction saleTransaction : result) {
                List<String> saleTransactionDataRecord = new ArrayList<String>();

                // Date (local time, not UTC)
                saleTransactionDataRecord.add(saleTransaction.getTransactionDateTime() == null ? ""
                        : fmt1.print(saleTransaction.getTransactionDateTime()));

                // Time (local time, not UTC)
                saleTransactionDataRecord.add(saleTransaction.getTransactionDateTime() == null ? ""
                        : fmt2.print(saleTransaction.getTransactionDateTime()));

                // Invoice
                saleTransactionDataRecord.add(saleTransaction.getInvoiceNumber());

                // Amount
                saleTransactionDataRecord
                        .add(saleTransaction.getAmount() == null ? "" : "$" + saleTransaction.getAmount().toString());

                // Result
                saleTransactionDataRecord.add(internalStatusCodeService
                        .getLetterFromStatusCodeForSaleTransactions(saleTransaction.getInternalStatusCode()));

                // Error Message
                saleTransactionDataRecord.add(saleTransaction.getInternalStatusDescription());

                csvFilePrinterContent.printRecord(saleTransactionDataRecord);
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }

        return file;
    }
}
