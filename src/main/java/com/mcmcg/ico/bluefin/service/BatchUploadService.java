package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final Object[] TRANSACTIONS_FILE_HEADER = { "#", "Date", "Time", "Invoice", "Amount", "Result",
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
        BatchUpload batchUpload = createBasicBatchUpload(username, fileName, lines);
        batchUpload = batchUploadRepository.save(batchUpload);

        // call new application to process file content (fileStream)
        String response = HttpsUtil.sendPostRequest(batchProcessServiceUrl + batchUpload.getBatchUploadId().toString(),
                fileStream);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JodaModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return objectMapper.readValue(response, BatchUpload.class);
        } catch (IOException e) {
            LOGGER.error("Unable to parse ACF batch process service response.", e);
            throw new CustomException("Unable to parse ACF batch process service response.");
        }
    }

    private BatchUpload createBasicBatchUpload(String username, String fileName, int lines) {
        BatchUpload batchUpload = new BatchUpload();
        batchUpload.setDateUploaded(new DateTime());
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");
        String date = fmt.print(new DateTime().toDateTime(DateTimeZone.UTC));
        batchUpload.setName(date);
        batchUpload.setFileName(fileName);
        batchUpload.setUpLoadedBy(username);
        batchUpload.setBatchApplication("Latitude");
        batchUpload.setProcessStart(new DateTime());
        batchUpload.setNumberOfTransactions(lines);
        return batchUpload;
    }

    public File getBatchUploadTransactionsReport(Long batchUploadId) throws IOException {
        List<SaleTransaction> result = null;
        File file = null;

        if (batchUploadId == null) {
            result = saleTransactionRepository.findAll();
        } else {
            result = saleTransactionRepository.findByBatchUploadId(batchUploadId);
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
            csvFilePrinter.printRecord(TRANSACTIONS_FILE_HEADER);

            // TransactionDateTime needs to be split into two parts.
            DateTimeFormatter fmt1 = DateTimeFormat.forPattern("MM/dd/yyyy");
            DateTimeFormatter fmt2 = DateTimeFormat.forPattern("hh:mm:ss.SSa");
            Integer count = 1;
            // Write a new transaction object list to the CSV file
            for (SaleTransaction saleTransaction : result) {
                List<String> saleTransactionDataRecord = new ArrayList<String>();
                saleTransactionDataRecord.add(count.toString());

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
                saleTransactionDataRecord.add(saleTransaction.getInternalStatusCode());

                // Error Message
                saleTransactionDataRecord.add(saleTransaction.getInternalStatusDescription());

                csvFilePrinter.printRecord(saleTransactionDataRecord);
                count++;
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }

        return file;
    }
}
