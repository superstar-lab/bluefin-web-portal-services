package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.SaleTransaction;

public interface SaleTransactionDAO {
	List<SaleTransaction> findAll();

	SaleTransaction findByApplicationTransactionId(String transactionId);

	SaleTransaction findByProcessorTransactionId(String transactionId);
	
	List<SaleTransaction> findByBatchUploadId(Long batchUploadId);
}
