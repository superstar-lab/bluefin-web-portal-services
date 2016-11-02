package com.mcmcg.ico.bluefin.persistent.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcmcg.ico.bluefin.persistent.RefundTransaction;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {
    public RefundTransaction findByApplicationTransactionId(final String transactionId);
    public RefundTransaction findByProcessorTransactionId(String transactionId);
}