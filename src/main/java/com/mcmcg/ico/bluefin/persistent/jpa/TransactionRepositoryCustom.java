package com.mcmcg.ico.bluefin.persistent.jpa;

import java.text.ParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.persistent.SaleTransaction;

public interface TransactionRepositoryCustom {

    public Page<SaleTransaction> findTransaction(String search, PageRequest page) throws ParseException;
    public List<SaleTransaction> findTransactionsReport(String search) throws ParseException;

}
