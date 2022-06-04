package com.mcmcg.ico.bluefin.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mcmcg.ico.bluefin.BluefinWebPortalConstants;
import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import com.mcmcg.ico.bluefin.model.UpdateInfo;
import com.mcmcg.ico.bluefin.repository.TransactionDAO;
import com.mcmcg.ico.bluefin.service.PropertyService;
import com.mcmcg.ico.bluefin.service.TransactionUpdateService;
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
    public List<SaleTransactionInfo> getTransactionsFromUpdates(List<UpdateInfo> updates) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. updates size: {}", updates.size());
        List<SaleTransactionInfo> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            String[] tokens = updates.stream().map(UpdateInfo::getToken).collect(Collectors.toList()).toArray(new String[0]);
            List<SaleTransactionInfo> trans = transactionDAO.getTransactionsFromUpdates(tokens);

            for (UpdateInfo update : updates) {
                //Filter the potential valid transactions, depending on the info given
                List<SaleTransactionInfo> transUpdate = trans.stream().filter(t ->
                        update.getToken().equals(t.getToken()) && update.getApplication().equalsIgnoreCase(t.getApplication())
                                && update.getAccountNo().equalsIgnoreCase(t.getAccountNo())
                ).collect(Collectors.toList());

                //Set the update reason from the coming update to the potential valid transactions to return
                transUpdate.stream().forEach(x -> x.setUpdateReason(update.getUpdateReason()));

                String dateUpdate = update.getUpdateDate();
                if (dateUpdate.contains(".")) {
                    dateUpdate = dateUpdate.substring(0, dateUpdate.indexOf("."));
                }

                LocalDateTime dateTimeUpdate = LocalDateTime.parse(dateUpdate, formatter);
                //Add the valid transactions to the return variable, if the date of the transaction if after the date of the update
                transactions.addAll(transUpdate.stream()
                        .filter(t -> {
                            String dateTran = t.getTransactionDateTime().substring(0, t.getTransactionDateTime().indexOf("."));
                            LocalDateTime dateTimeTran = LocalDateTime.parse(dateTran, formatter);
                            if (dateTimeTran.isAfter(dateTimeUpdate)) {
                                return true;
                            }
                            return false;
                        }).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("TransactionUpdateServiceImpl -> getTransactionsFromUpdates. Error getting the transactions, error: {}", e.toString());
        }

        return transactions;
    }

    @Override
    public Map<String, Long> getTransactionsFromUpdatesMetrics(List<UpdateInfo> updates) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdatesMetrics. updates size: {}", updates.size());
        Map<String, Long> metrics = new HashMap<>();

        List<SaleTransactionInfo> transactions = getTransactionsFromUpdates(updates);
        long approved = transactions.stream().filter(a -> BluefinWebPortalConstants.APPROVE_STATUS_CODE.equals(a.getInternalStatusCode())).count();
        long declined = transactions.stream().filter(a -> !BluefinWebPortalConstants.APPROVE_STATUS_CODE.equals(a.getInternalStatusCode())).count();

        metrics.put(BluefinWebPortalConstants.APPROVED_TRANSACTIONS_METRIC, approved);
        metrics.put(BluefinWebPortalConstants.DECLINED_TRANSACTIONS_METRIC, declined);
        metrics.put("Total Transactions", (long) transactions.size());

        return metrics;
    }

    @Override
    public ResponseEntity<String> getTransactionsFromUpdateReport(List<UpdateInfo> updates, HttpServletResponse response) {
        log.info("TransactionUpdateServiceImpl -> getTransactionsFromUpdateReport. updates size: {}", updates.size());

        List<SaleTransactionInfo> transactions = getTransactionsFromUpdates(updates);

        String reportPath = propertyService.getPropertyValue(BluefinWebPortalConstants.REPORT_PATH);
        File file = FileUtil.generateCSVFile(reportPath, FILE_HEADER, prepareDataForTransactionsReport(transactions));

        return FileUtil.deleteTempFile(file, response);
    }

    private Map<Integer, List<String>> prepareDataForTransactionsReport(List<SaleTransactionInfo> transactions) {
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
            tranDataRecord.add(transaction.getTransactionDateTime());
            tranDataRecord.add(transaction.getApplication());
            tranDataRecord.add(transaction.getUpdateReason());

            data.put(count, tranDataRecord);
            count++;
        }

        return data;
    }

}
