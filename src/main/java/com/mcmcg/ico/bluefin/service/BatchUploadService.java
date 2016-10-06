package com.mcmcg.ico.bluefin.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

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

import com.mcmcg.ico.bluefin.persistent.BatchUpload;
import com.mcmcg.ico.bluefin.persistent.jpa.BatchUploadRepository;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomNotFoundException;
import com.mcmcg.ico.bluefin.service.util.querydsl.QueryDSLUtil;

@Service
@Transactional
public class BatchUploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchUploadService.class);

    @Autowired
    private BatchUploadRepository batchUploadRepository;
    // Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    // CSV file header
    private static final Object[] FILE_HEADER = { "Batch Upload Id", "File Name", "Name", "Date Uploaded",
            "Batch Application", "Number Of Transactions", "Transactions Processed", "Approved Transactions",
            "Declined Transactions", "Error Transactions", "Rejected Transactions", "Process Start", "Process End",
            "UpLoadedBy" };

    @Value("${bluefin.wp.services.batch.upload.report.path}")
    private String reportPath;

    public BatchUpload getBatchUploadById(Long id) {
        BatchUpload batchUpload = batchUploadRepository.findOne(id);

        if (batchUpload == null) {
            LOGGER.error("Unable to find batch upload with id = {}", id);
            throw new CustomNotFoundException(String.format("Unable to find batch upload with id = [%s]", id));
        }

        return batchUpload;
    }

    public Iterable<BatchUpload> getAllBatchUploads(Integer page, Integer size, String sort) {
        Page<BatchUpload> result = batchUploadRepository.findAll(QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
    }

    public Iterable<BatchUpload> getBatchUploadsFilteredByNoofdays(Integer page, Integer size, String sort,
            Integer noofdays) {
        DateTime dateBeforeNoofdays = new DateTime().toDateTime(DateTimeZone.UTC).minusDays(noofdays);
        Page<BatchUpload> result = batchUploadRepository.findByDateUploadedAfter(dateBeforeNoofdays,
                QueryDSLUtil.getPageRequest(page, size, sort));
        if (page > result.getTotalPages() && page != 0) {
            throw new CustomNotFoundException("Unable to find the page requested");
        }

        return result;
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

                batchUploadDataRecord.add(batchUpload.getProcesStart() == null ? ""
                        : fmt.print(batchUpload.getProcesStart().toDateTime(DateTimeZone.UTC)));
                batchUploadDataRecord.add(batchUpload.getProcesEnd() == null ? ""
                        : fmt.print(batchUpload.getProcesEnd().toDateTime(DateTimeZone.UTC)));

                batchUploadDataRecord.add(batchUpload.getUpLoadedBy());

                csvFilePrinter.printRecord(batchUploadDataRecord);
            }
            LOGGER.info("CSV file report was created successfully !!!");
        }
        return file;
    }

}
