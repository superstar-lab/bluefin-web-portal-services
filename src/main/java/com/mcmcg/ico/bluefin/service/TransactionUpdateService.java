package com.mcmcg.ico.bluefin.service;

import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import com.mcmcg.ico.bluefin.model.UpdateInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface TransactionUpdateService {
    Page<SaleTransactionInfo> getTransactionsFromUpdate(String updateRequest, String application, String updateDate, PageRequest page);
    List<SaleTransactionInfo> getTransactionsFromUpdates(List<UpdateInfo> updates);
    Map<String, Long> getTransactionsFromUpdatesMetrics(List<UpdateInfo> updates);
    ResponseEntity<String> getTransactionsFromUpdateReport(List<UpdateInfo> updates, HttpServletResponse response);
}
