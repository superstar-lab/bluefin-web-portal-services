package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.RefundTransaction;

public interface RefundTransactionDAO {
	List<RefundTransaction> findAll();

	RefundTransaction findByApplicationTransactionId(final String transactionId);

	RefundTransaction findByProcessorTransactionId(String transactionId);
}
