package com.mcmcg.ico.bluefin.repository;

import com.mcmcg.ico.bluefin.model.SaleTransactionInfo;
import com.mcmcg.ico.bluefin.model.UpdateInfo;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

public interface TransactionDAO {
    List<SaleTransactionInfo> getTransactionsFromUpdate(UpdateInfo update, PageRequest page);
    long getTransactionsUpdateCount(UpdateInfo update);
    List<SaleTransactionInfo> getTransactionsFromUpdates(String[] tokens, String startTime);
}
