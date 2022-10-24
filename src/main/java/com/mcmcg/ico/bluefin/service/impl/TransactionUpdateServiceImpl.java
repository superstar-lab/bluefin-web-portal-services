package com.mcmcg.ico.bluefin.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import com.mcmcg.ico.bluefin.model.UpdateInfo;
import com.mcmcg.ico.bluefin.repository.TransactionDAO;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.TransactionUpdateService;
import com.mcmcg.ico.bluefin.util.DateTimeUtil;
import com.mcmcg.ico.bluefin.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionUpdateServiceImpl implements TransactionUpdateService {

    private TransactionDAO transactionDAO;

    private PropertyService propertyService;

    private static final Object[] FILE_HEADER = { "#", "Bluefin transaction ID", "Account Number", "Amount", "Expiration Date", "Processor Status Description", "Token", "Transaction Date", "Legal Entity", "Update Reason"};

    private static final String STATUS_CODE = "statusCode";
    private static final String CC_CHANGED = "202";
    private static final String NEW_TOKEN = "newToken";
    private static final String TOKEN = "token";
    private static final String MCM_GEMINI = "MCM-GEMINI";
    private static final String ACCOUNT_ID = "accountID";
    private static final String CLIENT_ACCOUNT_ID = "clientAccountID";

    private static final String DATE_STRING_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_STRING_PATTERN);

    public TransactionUpdateServiceImpl(TransactionDAO transactionDAO, PropertyService propertyService) {
        this.transactionDAO = transactionDAO;
        this.propertyService = propertyService;
    }

    @Override
    public Page<SaleTransactionInfo> getTransactionsFromUpdate(String updateRequest,  String application, String updateDate, PageRequest page) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdate. updateRequest: {}, application: {}, updateDate: {}, page: {}",
                updateRequest, application, updateDate, page);
        List<SaleTransactionInfo> trans = new ArrayList<>();
        long count = 0;

        try {
            JsonObject jsonUpdateRequest = new Gson().fromJson(updateRequest, JsonObject.class);

            String status = jsonUpdateRequest.get(STATUS_CODE) != null ? jsonUpdateRequest.get(STATUS_CODE).getAsString() : StringUtils.EMPTY;

            // compare with 202 as it is the update when a new token gets generated
            String token;
            if (CC_CHANGED.equals(status)) {
                token = jsonUpdateRequest.get(NEW_TOKEN) != null ? jsonUpdateRequest.get(NEW_TOKEN).getAsString() : StringUtils.EMPTY;
            } else {
                token = jsonUpdateRequest.get(TOKEN) != null ? jsonUpdateRequest.get(TOKEN).getAsString() : StringUtils.EMPTY;
            }

            String accountNo;
            if (MCM_GEMINI.equals(application)) {
                accountNo = jsonUpdateRequest.get(ACCOUNT_ID) != null ? jsonUpdateRequest.get(ACCOUNT_ID).getAsString() : StringUtils.EMPTY;
            } else {
                accountNo = jsonUpdateRequest.get(CLIENT_ACCOUNT_ID) != null ? jsonUpdateRequest.get(CLIENT_ACCOUNT_ID).getAsString() : StringUtils.EMPTY;
            }

            UpdateInfo update = new UpdateInfo(token, application, accountNo, updateDate);

            trans = transactionDAO.getTransactionsFromUpdate(update, page);
            count = transactionDAO.getTransactionsUpdateCount(update);

        } catch (Exception e) {
            log.error("TransactionUpdateServiceImpl -> getTransactionsFromUpdate. Error getting the transactions, updateRequest: {}, application: {}, updateDate: {}, error: {}", updateRequest, application,
                    updateDate, e.toString());
        }

        return new PageImpl<>(trans,page,count);
    }

    @Override
    public List<SaleTransactionInfo> getTransactionsFromUpdates(List<UpdateInfo> updates, String fromDate, String timeZone) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. updates size: {}", updates.size());

        updates = updates.stream().distinct().collect(Collectors.toList());
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. clear duplicates size: {}", updates.size());

        String[] tokens = updates.stream().map(UpdateInfo::getToken).collect(Collectors.toList()).toArray(new String[0]);

        try {
            List<SaleTransactionInfo> transactions = new ArrayList<>();

            String startDate = DateTimeUtil.datetimeToUTC(fromDate, timeZone);
            List<SaleTransactionInfo> sales = transactionDAO.getTransactionsFromUpdates(tokens, startDate);
            log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. Sales from DB, : {}", sales.size());

            for (UpdateInfo update : updates) {

                LocalDateTime ensurebillUpdateDate = parseWithoutMillis(update.getUpdateDate());

                List<SaleTransactionInfo> salesUpdated =
                        sales.stream()
                        .filter(t -> update.getToken().equals(t.getToken()))
                        .filter(t -> parseWithoutMillis(t.getTransactionDateTime()).isAfter(ensurebillUpdateDate))
                                .collect(Collectors.toList());

                salesUpdated.stream().forEach(x -> x.setUpdateReason(update.getUpdateReason()));
                transactions.addAll(salesUpdated);
            }
            return transactions;
        } catch (Exception e) {
            log.error("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. Error getting the transactions, error: {}", e.toString());
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Map<String, Long> getTransactionsFromUpdatesMetrics(List<UpdateInfo> updates, String fromDate, String timeZone) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdatesMetrics. updates size: {}", updates.size());
        Map<String, Long> metrics = new HashMap<>();

        List<SaleTransactionInfo> transactions = getTransactionsFromUpdates(updates, fromDate, timeZone);
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdatesMetrics. transactions size: {}", transactions.size());

        long approved = transactions.stream().filter(a -> BluefinWebPortalConstants.APPROVE_STATUS_CODE.equals(a.getInternalStatusCode())).count();
        long declined = transactions.stream().filter(a -> !BluefinWebPortalConstants.APPROVE_STATUS_CODE.equals(a.getInternalStatusCode())).count();

        metrics.put(BluefinWebPortalConstants.APPROVED_TRANSACTIONS_METRIC, approved);
        metrics.put(BluefinWebPortalConstants.DECLINED_TRANSACTIONS_METRIC, declined);
        metrics.put("Total Transactions", (long) transactions.size());

        return metrics;
    }

    @Override
    public ResponseEntity<String> getTransactionsFromUpdateReport(List<UpdateInfo> updates, String fromDate, String timeZone, HttpServletResponse response) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdateReport. updates size: {}", updates.size());

        List<SaleTransactionInfo> transactions = getTransactionsFromUpdates(updates, fromDate, timeZone);

        String reportPath = propertyService.getPropertyValue(BluefinWebPortalConstants.REPORT_PATH);
        File file = FileUtil.generateCSVFile(reportPath, FILE_HEADER, prepareDataForTransactionsReport(transactions, timeZone));

        return FileUtil.deleteTempFile(file, response);
    }

    private Map<Integer, List<String>> prepareDataForTransactionsReport(List<SaleTransactionInfo> transactions, String timeZone) {
        log.info("Entering to TransactionServiceImpl -> prepareDataForTransactionsReport, transactions size: {}", transactions.size());
        Map<Integer, List<String>> data = new TreeMap<>();
        List<String> tranDataRecord;
        Integer count = 1;

        for (SaleTransactionInfo transaction : transactions) {
            tranDataRecord = new ArrayList<>();
            tranDataRecord.add(transaction.getId());
            tranDataRecord.add(transaction.getAccountNo());
            tranDataRecord.add(transaction.getChargeAmount());
            tranDataRecord.add(transaction.getExpDate());
            tranDataRecord.add(transaction.getStatus());
            tranDataRecord.add("'".concat(transaction.getToken()));

            String dateTime = transaction.getTransactionDateTime();
            dateTime = dateTime.contains(".") ? dateTime.substring(0, dateTime.indexOf(".")) : dateTime;
            dateTime = DateTimeUtil.datetimeToUTC(dateTime,timeZone,DATE_STRING_PATTERN);

            tranDataRecord.add(dateTime);
            tranDataRecord.add(transaction.getApplication());
            tranDataRecord.add(transaction.getUpdateReason());

            data.put(count, tranDataRecord);
            count++;
        }

        return data;
    }

    private LocalDateTime parseWithoutMillis(String textDate){
        String withoutMillis = textDate.contains(".") ? textDate.substring(0, textDate.indexOf(".")) : textDate;
        return LocalDateTime.parse(withoutMillis, FORMATTER);
    }
}
