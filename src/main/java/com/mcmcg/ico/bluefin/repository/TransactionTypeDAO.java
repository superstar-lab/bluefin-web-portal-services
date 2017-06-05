package com.mcmcg.ico.bluefin.repository;

import java.util.List;

import com.mcmcg.ico.bluefin.model.TransactionType;

public interface TransactionTypeDAO {
	List<TransactionType> findAll();

	TransactionType findByTransactionId(long transactionTypeId);

	TransactionType findByTransactionType(String type);
}
