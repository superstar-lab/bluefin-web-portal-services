package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import com.mcmcg.ico.bluefin.persistent.SaleTransaction;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long>,
        QueryDslPredicateExecutor<SaleTransaction>, TransactionRepositoryCustom {

    public SaleTransaction findByApplicationTransactionId(String transactionId);

    public Long countByPaymentProcessorRuleId(Long paymentProcessorRuleId);
}