package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.TransactionView;

public interface TransactionRepository
        extends JpaRepository<TransactionView, Long>, QueryDslPredicateExecutor<TransactionView> {
    public TransactionView findByTransactionId(String transactionId);

}