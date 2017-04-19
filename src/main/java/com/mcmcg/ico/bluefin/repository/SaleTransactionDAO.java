package com.mcmcg.ico.bluefin.repository;

import java.text.ParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.mcmcg.ico.bluefin.model.SaleTransaction;

public interface SaleTransactionDAO {
	List<SaleTransaction> findAll();

	SaleTransaction findByApplicationTransactionId(String transactionId);

	SaleTransaction findByProcessorTransactionId(String transactionId);

	
	List<SaleTransaction> findByBatchUploadId(Long batchUploadId);

	Page<SaleTransaction> findTransaction(String search, PageRequest pageRequest) throws ParseException;

	List<SaleTransaction> findTransactionsReport(String search) throws ParseException;
}
