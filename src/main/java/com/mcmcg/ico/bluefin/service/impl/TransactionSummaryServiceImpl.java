package com.mcmcg.ico.bluefin.service.impl;

import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.dto.DeclinedTranSummaryDTO;
import com.mcmcg.ico.bluefin.dto.TopTranSummaryDTO;
import com.mcmcg.ico.bluefin.model.ApprovedTranSummary;
import com.mcmcg.ico.bluefin.model.User;
import com.mcmcg.ico.bluefin.repository.PropertyDAO;
import com.mcmcg.ico.bluefin.repository.TransactionSummaryDAO;
import com.mcmcg.ico.bluefin.rest.controller.exception.CustomException;
import com.mcmcg.ico.bluefin.service.TransactionSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionSummaryServiceImpl implements TransactionSummaryService {

    private TransactionSummaryDAO transactionSummaryDAO;
    private PropertyDAO propertyDAO;

    private static final Object[] FILE_DECLINED_HEADER = { "Processor", "# Declined Transactions", "# Approved Transactions", "Declined Rate"};
    private static final Object[] FILE_APPROVED_HEADER = { "Legal Entity", "# Transactions", "Total Amount", "Processor"};
    private static final Object[] FILE_TOP_HEADER = { "Declined Reason", "# Transactions", "Rate", "Processor"};

    public TransactionSummaryServiceImpl(TransactionSummaryDAO transactionSummaryDAO, PropertyDAO propertyDAO) {
        this.transactionSummaryDAO = transactionSummaryDAO;
        this.propertyDAO = propertyDAO;
    }

    @Override
    public List<DeclinedTranSummaryDTO> declinedSummary(String fromDate, String toDate) {
        return transactionSummaryDAO.declinedSummary(fromDate, toDate, BluefinWebPortalConstants.DECLINE_STATUS_CODE);
    }

    @Override
    public Map<String, List<ApprovedTranSummary>> approvedSummary(String fromDate, String toDate) {
        log.info("TransactionSummaryServiceImpl -> approvedSummary. fromDate: {}, toDate: {}", fromDate, toDate);
        Map<String, List<ApprovedTranSummary>> approvedList;

        List<ApprovedTranSummary> approvedTran = transactionSummaryDAO.approvedSummary(fromDate, toDate);
        approvedList = approvedTran.stream()
         .collect(Collectors.groupingBy(ApprovedTranSummary::getProcessor));

        return approvedList;
    }

    @Override
    public Map<String, List<TopTranSummaryDTO>> topSummary(String top, String statusCode, String fromDate, String toDate) {
        log.info("TransactionSummaryServiceImpl -> topSummary. top: {}, statusCode: {}, fromDate: {}, toDate: {}", top, statusCode, fromDate, toDate);
        Map<String, List<TopTranSummaryDTO>> topList;

        List<TopTranSummaryDTO> topTran = transactionSummaryDAO.topSummary(top, statusCode, fromDate, toDate);
        topList = topTran.stream()
                .collect(Collectors.groupingBy(TopTranSummaryDTO::getProcessor));

        return topList;
    }

    @Override
    public File tranSummaryReport(String top, String statusCode, String fromDate, String toDate) throws IOException {
        log.info("TransactionSummaryServiceImpl -> tranSummaryReport. Entering to execute method tranSummaryReport  Top: {}, Status Code: {} fromDate: {}, toDate: {}"
                , top, statusCode, fromDate, toDate);
        List<DeclinedTranSummaryDTO> declinedParameter = declinedSummary(fromDate, toDate);
        Map<String, List<ApprovedTranSummary>> approvedParameter = approvedSummary(fromDate, toDate);
        Map<String, List<TopTranSummaryDTO>> topParameter = topSummary(top, statusCode, fromDate, toDate);

        String reportPath = propertyDAO.getPropertyValue("REPORT_PATH");
        // Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
        File file = createFileToPrepareReport(reportPath);
        // initialize FileWriter object
        try (FileWriter fileWriter = new FileWriter(file);
             CSVPrinter csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);) {
            // initialize CSVPrinter object
            // Create CSV file header
            csvFilePrinter.printRecord("APPROVED TRANSACTIONS");
            csvFilePrinter.printRecord(FILE_APPROVED_HEADER);
            log.info("TransactionSummaryServiceImpl -> tranSummaryReport, Approved Result size : {} ", approvedParameter.size());
            // Write a new transaction object list to the CSV file
            approvedParameter.forEach((k, v) -> {
                try {
                    for (ApprovedTranSummary approved : v) {
                        List<String> transactionDataRecord = prepareDataForTransactionReportApproved(approved);

                        csvFilePrinter.printRecord(transactionDataRecord);

                    }
                    List<String> grandTotal = calculateGrandTotal(v);
                    csvFilePrinter.printRecord(grandTotal);
                    csvFilePrinter.printRecord(StringUtils.EMPTY);
                } catch (IOException e) {
                    log.error("TransactionSummaryServiceImpl -> tranSummaryReport. Error while printing record to excel for Approved Transactions summary : {}", e.toString());
                }
            });
            csvFilePrinter.printRecord(StringUtils.EMPTY);
            csvFilePrinter.printRecord(StringUtils.EMPTY);
            // Create CSV file header
            csvFilePrinter.printRecord("DECLINED TRANSACTIONS");
            csvFilePrinter.printRecord(FILE_DECLINED_HEADER);
            log.info("TransactionSummaryServiceImpl -> tranSummaryReport, Declined Result size : {} ", declinedParameter.size());
            // Write a new transaction object list to the CSV file
            for (DeclinedTranSummaryDTO declined : declinedParameter) {
                List<String> transactionDataRecord = prepareDataForTransactionReport(declined);
                csvFilePrinter.printRecord(transactionDataRecord);
            }
            csvFilePrinter.printRecord(StringUtils.EMPTY);
            csvFilePrinter.printRecord(StringUtils.EMPTY);
            csvFilePrinter.printRecord("TOP SUMMARY DECLINE REASONS");
            csvFilePrinter.printRecord(FILE_TOP_HEADER);
            log.info("TransactionSummaryServiceImpl -> tranSummaryReport, Top Declined Result size : {} ", topParameter.size());
            // Write a new transaction object list to the CSV file
            topParameter.forEach((k, v) -> {
                try {
                    for (TopTranSummaryDTO topSummary : v) {
                        List<String> transactionDataRecord = prepareDataForTransactionReportTop(topSummary);

                        csvFilePrinter.printRecord(transactionDataRecord);
                    }
                    csvFilePrinter.printRecord(StringUtils.EMPTY);
                } catch (IOException e) {
                    log.error("TransactionSummaryServiceImpl -> tranSummaryReport. Error while printing record to excel for Top Transactions Summary : {}", e.toString());
                }
            });

            log.info("TransactionSummaryServiceImpl -> tranSummaryReport, CSV file report was created successfully !!!");
        }
        return file;

    }

    private List<String> calculateGrandTotal(List<ApprovedTranSummary> approvedGrandTotal) {
        List<String> dataRecord = new ArrayList<>();
        dataRecord.add("Grand Total");
        dataRecord.add(String.valueOf(approvedGrandTotal.stream().map(ApprovedTranSummary::getNumberTransactions).mapToInt(Integer::parseInt).sum()));
        String result = String.format("%.2f", approvedGrandTotal.stream().map(ApprovedTranSummary::getTotalAmount).mapToDouble(Double::parseDouble).sum());
        dataRecord.add(result);

        return dataRecord;
    }

    private List<String> prepareDataForTransactionReportTop(TopTranSummaryDTO topSummaryDTO) {
        List<String> dataRecord = new ArrayList<>();
        dataRecord.add(topSummaryDTO.getDeclineReason());
        dataRecord.add(topSummaryDTO.getTotalTransactions());
        dataRecord.add(topSummaryDTO.getRate());
        dataRecord.add(topSummaryDTO.getProcessor());

        return dataRecord;
    }

    private List<String> prepareDataForTransactionReportApproved(ApprovedTranSummary approvedDTO) {
        List<String> dataRecord = new ArrayList<>();
        dataRecord.add(approvedDTO.getLegalEntity());
        dataRecord.add(approvedDTO.getNumberTransactions());
        dataRecord.add(approvedDTO.getTotalAmount());
        dataRecord.add(approvedDTO.getProcessor());

        return dataRecord;
    }

    private List<String> prepareDataForTransactionReport(DeclinedTranSummaryDTO declinedDTO) {
        List<String> dataRecord = new ArrayList<>();
        dataRecord.add(declinedDTO.getProcessor());
        dataRecord.add(declinedDTO.getDeclined());
        dataRecord.add(declinedDTO.getApproved());
        dataRecord.add(declinedDTO.getRate());

        return dataRecord;
    }

    private File createFileToPrepareReport(String reportPath) {
        log.info("TransactionSummaryServiceImpl --> createFileToPrepareReport");
        try {
            File dir = new File(reportPath);
            dir.mkdirs();
            File file = new File(dir, UUID.randomUUID() + ".csv");
            boolean flag;
            flag = file.createNewFile();
            if (flag) {
                log.info("TransactionSummaryServiceImpl --> createFileToPrepareReport, Report file Created {}", file.getName());
            }
            return file;
        } catch (Exception e) {
            log.error("TransactionSummaryServiceImpl --> createFileToPrepareReport ReportPath:{}, Error:{}", reportPath, e.toString());
            throw new CustomException("Error creating file: " + reportPath + UUID.randomUUID() + ".csv");
        }
    }
}
