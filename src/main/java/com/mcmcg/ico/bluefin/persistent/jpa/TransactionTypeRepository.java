package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.TransactionType;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long> {
    public TransactionType findByTransactionTypeName(String transactionTypeName);
}
